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

package org.apius.server.identity.session.filter;

import org.apius.server.identity.session.Session;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ResourceException;
import org.restlet.security.Authorizer;

/**
 * <p>
 * Filter to authorize an already authenticated user making the API call. If a
 * stateful session is being used to access the API, all calls to operate on 
 * protected resources within the scope of the API must include an Authorization 
 * header with a session token as its value. The syntax will be as follows:
 * </p>
 * <p>
 * <code>Authorization: APIUS token=AQIC5wM2LY4SfcwGgIvSF9oEp5[...]</code>
 * </p>
 * <p>
 * The object instantiating this <code>SessionAuthorizer</code> class should 
 * set its <code>setNext(Restlet next)</code> method to an instance of the <code>
 * UniformResource</code> class representing the resource to be operated on.
 * </p>
 * <p>
 * If authorized, the request may be passed to the next Restlet. If not, the
 * request status code must be set to 403 (Forbidden).
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public class SessionAuthorizer extends Authorizer {
	
    private Session sessionProvisionerProxy;
    
    /**
     * Constructor
     * 
     * @param sessionProxy
     */
    public SessionAuthorizer(Session sessionProvisionerProxy) {
    	this.sessionProvisionerProxy = sessionProvisionerProxy;
    }
    
    @Override
    protected boolean authorize(Request request, Response response) {
    	return (isAuthorized(request, response));
    }
    
    private boolean isAuthorized(Request request, Response response) {
        boolean isAuthorized = false;
        
        try {
            if (request.getChallengeResponse() != null) {
                isAuthorized = sessionProvisionerProxy.isAuthorized(request.getOriginalRef().toString(), 
                                                                    request.getMethod().getName(), 
                                                                    request.getChallengeResponse().getRawValue());
            }
        } catch (ResourceException e) {
            response.setStatus(e.getStatus(), e.getMessage());
        }
        
        return isAuthorized;
    }

}
