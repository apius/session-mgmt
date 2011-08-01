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

package org.apius.server.identity.session.server;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

/**
 * <p>
 * Stateful session servers are bound to expose some attributes about the user
 * associated with the session. This class provides a consistent interface 
 * for performing HTTP operations against the resource represented by these 
 * attributes regardless of the underlying implementation of the particular 
 * session provisioner.
 * </p>
 * <p>
 * The object implementing the <code>SessionProxyProvisioner</code> interface 
 * is the session provisioner mediator. Not exposing this proxy object to the 
 * public API allows us to 1) design our own RESTful interface for managing 
 * sessions across our API and 2) keep the public contract consistent even if 
 * more than one stateful session provisioner (more than one implementation of 
 * our session interface) is introduced into the scope of the API.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public interface SessionAttributes {
	
    /**
     * Requests session provisioner to respond with attributes associated with
     * the session represented by the token.
     * 
     * @return Representation
     */
    @Get
    abstract Representation getAttributes();
	
}
