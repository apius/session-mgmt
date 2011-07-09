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

import org.apius.server.identity.session.Session;
import org.apius.server.identity.session.SessionCookieSettings;
import org.apius.server.identity.session.SessionProvisionerProxy;
import org.apius.server.identity.session.filter.SessionAuthenticator;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CookieSetting;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

/**
 * <p>
 * Implementation of the <code>Session</code> interface for the <a href="forgerock.com/openam.html">
 * OpenAM</a> session provisioner, authenticator and authorizer application.
 * </p>
 * <p>
 * <b><em>Public API Contract</em></b>
 * </p>
 * <p>
 * <b>POST</b>
 * </p>
 * <p>
 * Looks for the username and password in the request. Two methods of passing those values are 
 * supported. The credentials may be passed in as parameters in the query string of the request 
 * as in:
 * </p>
 * <p>
 * <code>POST /{service_url}?username=pmorris&password=secret</code>
 * </p>
 * <p>
 * The credentials may also be sent following the w3 standard for HTTP Basic Authentication, 
 * namely a colon-delimited ("pmorris:secret") and Base64 encoded string pair, as in:
 * </p>
 * <p>
 * <code>Authorization: Basic cG1vcnJpczpzZWNyZXQ=</code>
 * </p>
 * <p>
 * In either case the method extracts the credentials and passes them into the object implementing
 * the <code>SessionProvisionerProxy</code> interface to request a session token to be provisioned.
 * </p>
 * <p>
 * If a token is provisioned, it will be sent back to the client in a cookie along with an HTTP 201
 * status code. The purpose of the cookie is NOT however to be sent along with each request across 
 * the entire scope of the API. Its sole purpose is to serve as a client-side container to store the 
 * session token value. It's the Authorization header that will be checked by the Filter objects 
 * across our API's scope to grant or deny access to resources.
 * </p>
 * <p>
 * The object implementing the <code>SessionProxyProvisioner</code> interface is the session 
 * provisioner mediator. Not exposing this proxy object to the public API allows us to 1) design 
 * our own RESTful interface for managing sessions across our API and 2) keep the public contract
 * consistent even if more than one stateful session provisioner (more than one implementation of
 * our session interface) is introduced into the scope of the API.
 * </p>
 * <p>
 * <b>PUT</b>
 * </p>
 * <p>
 * The token value must be included in the Authorization header of the request as in:
 * </p>
 * <p>
 * <code>Authorization: APIUS token=AQIC5wM2LY4SfcwGgIvSF9oEp5y7rZl[...]</code>
 * </p>
 * <p>
 * If it finds this value then the user is requesting for the session to be refreshed.
 * </p>
 * <p>
 * <b>DELETE</b>
 * </p>
 * <p>
 * Invalidates the session represented by the token passed in as a parameter, effectively logging 
 * out the user. The token value must be included in the Authorization header of the request 
 * as in:
 * </p>
 * <p>
 * <code>Authorization: APIUS token=AQIC5wM2LY4SfcwGgIvSF9oEp5[...]</code>
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public class SessionImpl extends ServerResource implements Session {
	
    private String[] credentials;
    private SessionProvisionerProxy sessionProvisionerProxy;
    private SessionAuthenticator sessionAuthenticator;
    private CookieSetting cookieSetting;
    
    /**
     * Constructor
     * 
     * @param sessionProvisionerProxy
     * @param sessionAuthenticator
     */
    public SessionImpl(SessionProvisionerProxy sessionProvisionerProxy, SessionAuthenticator sessionAuthenticator) {
        this.sessionProvisionerProxy = sessionProvisionerProxy;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    /**
     * POST operation used to request the session provisioner to create a session
     * and return a token representing that session to the client.
     * 
     * @return void
     */
    public void createSession() {
        extractCredentialsFromRequest();

        if (credentialsAreNotNull()) {
            handleCreateSession();
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }
    
    private void extractCredentialsFromRequest() {
        credentials = new String[2];
        
        if (requestHasCredentialsAsQueryParameters()) {
            credentials[0] = getQuery().getFirstValue("username", true);
            credentials[1] = getQuery().getFirstValue("password", true);
        } else if (requestHasCredentialsInAuthHeader()) {
            credentials[0] = getChallengeResponse().getIdentifier();
            credentials[1] = new String(getChallengeResponse().getSecret());
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }
    
    private boolean requestHasCredentialsAsQueryParameters() {	
        return (getQuery().getFirst("username") != null && getQuery().getFirst("password") != null) ? true : false;
    }
    
    private boolean requestHasCredentialsInAuthHeader() {
        return (getChallengeResponse() != null && getChallengeResponse().getScheme().equals(ChallengeScheme.HTTP_BASIC)) ? true : false;
    }
    
    private boolean credentialsAreNotNull() {
        return (credentials[0] != null && credentials[1] != null) ? true : false;
    }
    
    private void handleCreateSession() {
        String token = "";
        
        try {
            token = sessionProvisionerProxy.createSession(credentials[0], credentials[1]).getText().replaceFirst("token.id=", "");
            
            if (!token.isEmpty()) {
                setStatus(Status.SUCCESS_CREATED);
                addCookieSetting(token);     
            }	
        } catch (ResourceException e) {
            handleResourceException(e);
        } catch (Exception e) {
            handleException(e);
        }	
    }
    
    private void addCookieSetting(String token) {
        cookieSetting.setVersion(SessionCookieSettings.version);
        cookieSetting.setName(SessionCookieSettings.name);
        cookieSetting.setValue("token=" + token);
        cookieSetting.setPath(getOriginalRef().getPath());
        cookieSetting.setComment(SessionCookieSettings.comment);
        cookieSetting.setMaxAge(SessionCookieSettings.maxAge);
        cookieSetting.setSecure(SessionCookieSettings.secure);
        cookieSetting.setAccessRestricted(SessionCookieSettings.accessRestricted);
        
        Series<CookieSetting> cookieSettings = this.getCookieSettings();
        cookieSettings.clear();
        cookieSettings.add(cookieSetting);
        this.setCookieSettings(cookieSettings);
    }
    
    /**
     * Public CookieSetting setter.
     * 
     * @param cookieSetting
     * @return void
     */
    public void setCookieSetting(CookieSetting cookieSetting) {
        this.cookieSetting = cookieSetting;
    }
    
    /**
     * PUT operation verifies that the session associated with the token is valid but
     * also refreshes the inactive timeout of the session.
     * 
     * @return void
     */
    public void validateSessionToken() {
        if (getChallengeResponse() != null) {
            try {
                sessionProvisionerProxy.isAuthenticated(getChallengeResponse().getRawValue());
            } catch (ResourceException e) {
                handleResourceException(e);
            } catch (Exception e) {
                handleException(e);
            }	
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }
    
    /**
     * DELETE operation to invalidate the session and effectively log out the
     * user.
     * 
     * @return void
     */
    public void logout() {
        if (getChallengeResponse() != null) {
            try {
                sessionProvisionerProxy.logout(getChallengeResponse().getRawValue());
            } catch (ResourceException e) {
                handleResourceException(e);
            } catch (Exception e) {
                handleException(e);
            }	
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }
    
    private void handleResourceException(ResourceException e) {
        if (e.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            sessionAuthenticator.challenge(getResponse(), false);
        } else {
            setStatus(e.getStatus(), e.getMessage());
        }
    }
    
    private void handleException(Exception e) {
        setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
    }
    
}
