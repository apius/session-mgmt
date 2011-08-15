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

import org.apius.server.identity.session.SessionAuthenticatorHelper;
import org.apius.server.identity.session.SessionVerifier;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.ChallengeAuthenticator;

/**
 * <p>
 * Filter to authenticate the user making the API call. If a stateful session 
 * is being used to access the API, all calls to operate on protected resources 
 * within the scope of the API must include an Authorization header with a session 
 * token as its value. The syntax will be as follows:
 * </p>
 * <p>
 * <code>Authorization: APIUS token=AQIC5wM2LY4SfcwGgIvSF9oEp5[...]</code>
 * </p>
 * <p>
 * If access to the resource requires authorization (i.e. not just authentication), 
 * the object instantiating this <code>SessionAuthenticator</code> must set its 
 * <code>setNext(Restlet next)</code> method to an instance of the <code>
 * SessionAuthorizer</code> class.
 * </p>
 * <p>
 * If authenticated, the request may be passed to the next Restlet, if not a
 * challenge response with a WWW-Authenticate header must be sent to the client 
 * and the request status code must be set to 401 (Unauthorized).
 * </p>
 * 
 * @author Paul Morris
 *  
 */
public class SessionAuthenticator extends ChallengeAuthenticator {
    
    /**
     * Constructor
     * 
     * @param context
     * @param realm
     * @param verifier
     * @param sessionProxy
     */
    public SessionAuthenticator(Context context, String realm, SessionVerifier verifier) {
        super(context, false, SessionAuthenticatorHelper.APIUS, realm, verifier);
    }
    
    @Override
    protected int authenticated(Request request, Response response) {
        return super.authenticated(request, response);
    }
	
}