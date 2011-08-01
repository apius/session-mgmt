/**
 * Copyright 2010-2011 apius.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apius.server.identity.session.client.openam;

import java.io.IOException;

import org.apius.server.identity.session.client.SessionProvisionerProxy;
import org.restlet.data.Status;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.xml.sax.SAXException;

/**
 * <p>
 * Implementation of the <code>SessionProvisionerProxy</code> interface for the 
 * <a href="forgerock.com/openam.html">OpenAM</a> session provisioner, authenticator 
 * and authorizer application.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public final class SessionProvisionerProxyImpl extends ClientResource implements SessionProvisionerProxy {
	
    private ResponseHelper responseHelper;
    
    /**
     * Constructor
     * 
     * @param baseUri
     * @param responseHelper
     */
    public SessionProvisionerProxyImpl(String baseUri, ResponseHelper responseHelper) {
        super(baseUri);
        this.responseHelper = responseHelper;
    }
    
    /**
     * Request OpenAM to create session and return token representing that session.
     * 
     * @param username
     * @param password
     * @return StringRepresentation
     * @throws ResourceException
     * @throws IOException
     */
    public StringRepresentation createSession(String username, String password) throws ResourceException, IOException {
        setReference(getReference().
                addSegment("identity").
                addSegment("authenticate").
                addQueryParameter("username", username).
                addQueryParameter("password", password));
    
        // DO NOT call this service with a GET. Elevating this to a POST will
        // prevent OpenAM from logging the sensitive username and password
        // parameters.
        //
        // http://blogs.sun.com/docteger/entry/opensso_entitlements_service_rest_interfaces
        return new StringRepresentation(this.post(null).getText());
    }
    
    /**
     * This call not only verifies that the session associated with the token is valid but
     * also refreshes the inactive timeout of the session.
     * 
     * @param token
     * @return boolean
     * @throws ResourceException
     * @throws IOException
     */
    public boolean isAuthenticated(String token) throws ResourceException, IOException {   
        setReference(getReference().
                addSegment("identity").
                addSegment("isTokenValid").
                addQueryParameter("tokenid", token));

        return responseHelper.extractBooleanFromResponseString(this.get().getText());
    }
    
    /**
     * Requests OpenAM to check whether its internal policies allow the user represented
     * by the session token to perform the HTTP operation against the resource addressed
     * by the URI.
     * 
     * @param uri
     * @param methodName
     * @param token
     * @return boolean
     * @throws ResourceException
     * @throws IOException
     */
    public boolean isAuthorized(String uri, String methodName, String token) throws ResourceException, IOException {
        setReference(getReference().
                addSegment("identity").
                addSegment("authorize").
                addQueryParameter("uri", uri).
                addQueryParameter("action", methodName).
                addQueryParameter("subjectid", token));
    
        return responseHelper.extractBooleanFromResponseString(this.get().getText());
    }
    
    /**
     * Extracts the identifier (or username) from the response of the 
     * <code>getSessionAttributesResponseString</code> method.
     * 
     * @param token
     * @return String
     * @throws ResourceException
     * @throws IOException
     */
    public String getIdentifier(String token) throws ResourceException, IOException {
        return responseHelper.extractUsernameFromSessionAttributes(getSessionAttributesResponseString(token));
    }
    
    /**
     * Requests OpenAM to respond with all known attributes associated with the session
     * token. Uses the <code>ResponseHelper</code> object to convert the response string
     * to XML.
     * 
     * @param token
     * @return SaxRepresentation
     * @throws IOException 
     * @throws ResourceException 
     * @throws SAXException 
     */
    public SaxRepresentation getSessionAttributes(String token) throws ResourceException, IOException, SAXException {  
        return responseHelper.writeSessionAttributesXml(getSessionAttributesResponseString(token));
    }
    
    private String getSessionAttributesResponseString(String token) throws ResourceException, IOException {
        setReference(getReference().
                addSegment("identity").
                addSegment("attributes").
                addQueryParameter("subjectid", token));
        
        return this.get().getText();		
    }
    
    /**
     * Invalidates the session and effectively logs out the user.
     * 
     * @param token
     * @return void
     * @throws IOException
     */
    public void logout(String token) throws IOException {
        setReference(getReference().
                addSegment("identity").
                addSegment("logout").
                addQueryParameter("subjectid", token));
        
        try {
            this.get().getText();
        } catch (ResourceException e) {
            // If the user passes in an invalid session token to the logout service, 
            // OpenAM returns a 500. The APIUS framework prefers to send back a 401 
            // since the reality is that the user is trying to access a resource with 
            // an unauthorized credential. So we check the entity for "Invalid session 
            // ID" and if we find it we change the status code to 401 before throwing 
            // the exception back to our Session resource.
            if (getResponse().getEntityAsText().contains("Invalid session ID")) {
                e = new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
            throw e;
        }
    }
    
}