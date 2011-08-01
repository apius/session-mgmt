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

package org.apius.server.identity.session.client;

import org.restlet.representation.Representation;

/**
 * <p>
 * While classes implementing the <code>Session</code> interface are for the 
 * public API (accepting HTTP method calls), the classes implementing this 
 * <code>SessionProvisionerProxy</code> interface are for internal API calls.
 * </p>
 * <p>
 * Since the proxy is acting as an HTTP client it makes sense to throw exceptions 
 * back to the caller since the caller is acting as an HTTP server and can 
 * handle the exceptions and appropriately modify the HTTP response to its 
 * requesting client.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public interface SessionProvisionerProxy {
	
    /**
     * Takes an identifier/secret pair (i.e. username/password) and requests the 
     * session provisioner to create a session on behalf of the user.
     * 
     * @param identifier
     * @param secret
     * @return Representation
     * @throws Exception
     */
    abstract Representation createSession(String identifier, String secret) throws Exception;
    
    /**
     * In addition to validating that the session token is valid, this call may also be 
     * used to refresh the session represented by the token.
     * 
     * @param token
     * @return boolean
     * @throws Exception
     */
    abstract boolean isAuthenticated(String token) throws Exception;
    
    /**
     * Requests session provisioner (and/or related implementation engine) to check whether 
     * the user represented by the session token is authorized to perform the HTTP operation 
     * against the resource at the URI.
     * 
     * @param uri
     * @param methodName
     * @param token
     * @return boolean
     * @throws Exception
     */
    abstract boolean isAuthorized(String uri, String methodName, String token) throws Exception;
    
    /**
     * Gets the identifier (i.e. username) associated with the session represented by the token.
     * 
     * @param token
     * @return String
     * @throws Exception
     */
    abstract String getIdentifier(String token) throws Exception;
    
    /**
     * Requests the session provisioner to respond with all known attributes associated  
     * with the session represented by the token.
     * 
     * @param token
     * @return Representation
     * @throws Exception
     */
    abstract Representation getSessionAttributes(String token) throws Exception;
    
    /**
     * Invalidates the session and effectively logs out user.
     * 
     * @param token
     * @return void
     * @throws Exception
     */
    abstract void logout(String token) throws Exception;

}
