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

package org.apius.server.identity.session;

import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

/**
 * 
 * <p>
 * Different session provisioners will have different interface contracts but 
 * APIUS encourages the use of a proxy implementation to sit behind this 
 * <code>Session</code> interface.
 * </p>
 * <p>
 * The object implementing the <code>SessionProxyProvisioner</code> interface 
 * is the session provisioner mediator. Not exposing this proxy object to the 
 * public API allows us to 1) design a consistent RESTful interface for managing 
 * sessions across our API and 2) keep the public contract consistent even if 
 * more than one stateful session provisioner (more than one implementation of
 * our session interface) is introduced into the scope of the API.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public interface Session {
	
    /**
     * Requests the session provisioner to create a session on behalf of the 
     * user whose credentials have been passed in the request.
     * 
     * @return void
     */
    @Post
    abstract void createSession();
	
    /**
     * This call authenticates the user represented by the session by validating 
     * the session token. The call may also be used to refresh the session
     * represented by the token.
     * 
     * @return void
     */
    @Put
    abstract void validateSessionToken();
	
    /**
     * This call invalidates the session represented by the token, essentially
     * logging out the user.
     * 
     * @return void
     */
    @Delete
    abstract void logout();
	
}
