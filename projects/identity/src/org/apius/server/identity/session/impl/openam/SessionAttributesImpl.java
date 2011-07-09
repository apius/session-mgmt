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

package org.apius.server.identity.session.impl.openam;

import org.apius.server.identity.session.SessionAttributes;
import org.apius.server.identity.session.SessionProvisionerProxy;
import org.apius.server.identity.session.filter.SessionAuthenticator;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * <p>
 * Implementation of the <code>SessionAttributes</code> interface for the 
 * <a href="forgerock.com/openam.html">OpenAM</a> session provisioner, 
 * authenticator and authorizer application.
 * </p>
 * <b><em>Public API Contract</em></b>
 * <p/>
 * <b>GET</b>
 * <p>
 * Returns attributes such as username, cn, etc. by querying the identity store. If a
 * stateful session is being used to access the API, the token value associated with 
 * the session whose attributes are being returned MUST either be included in the 
 * Authorization header of the request as in:
 * </p>
 * <code>Authorization: APIUS token=AQIC5wM2LY4SfcwGgIvSF9oEp5[...]</code>
 * <p>
 * Below is a sample of the XML model. All attributes follow this basic name value pattern. 
 * One or more values may be nested within certain attribute nodes.
 * </p>
 * &lt;session-attributes&gt;
 *   &lt;attribute name="username"&gt;
 *     &lt;value&gt;pmorris&lt;/value&gt;
 *   &lt;/attribute&gt;
 * &lt;/session-attributes&gt;
 * <p/>
 * 
 * @author Paul Morris
 *  
 */
public class SessionAttributesImpl extends ServerResource implements SessionAttributes {
    
    private SessionProvisionerProxy sessionProvisionerProxy;
    private SessionAuthenticator sessionAuthenticator;
    
    /**
     * Constructor
     * 
     * @param sessionProvisionerProxy
     * @param sessionAuthenticator
     */
    public SessionAttributesImpl(SessionProvisionerProxy sessionProvisionerProxy, SessionAuthenticator sessionAuthenticator) {
    	this.sessionProvisionerProxy = sessionProvisionerProxy;
    	this.sessionAuthenticator = sessionAuthenticator;
    }
    
    /**
     * Request OpenAM to respond with all known attributes associated with the session 
     * represented by the token.
     * 
     * @return Representation
     */
    public Representation getAttributes() {
        Representation representation = null;

        if (getChallengeResponse() != null) {	
            representation = handleGetAttributes(getChallengeResponse().getRawValue());
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        
        return representation;
    }
    
    private Representation handleGetAttributes(String token) {
    	Representation representation = null;
    	
    	try {
    	    representation = new DomRepresentation(
    	            sessionProvisionerProxy.getSessionAttributes(getChallengeResponse().getRawValue()));
    	} catch (ResourceException e) {
    	    if (e.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
    	        sessionAuthenticator.challenge(getResponse(), false);
    	    } else {
    	        setStatus(e.getStatus(), e.getMessage());
    	    }
    	} catch (Exception e) {
    	    setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
    	}
    	
    	return representation;
    }
	
}
