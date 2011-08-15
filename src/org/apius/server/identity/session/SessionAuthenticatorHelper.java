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

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;

/**
 * <p>
 * We want to set up a custom challenge scheme that we can register with 
 * the <a href="www.restlet.org">Restlet</a> framework. We'll depend on
 * its code to write our challenge headers.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public class SessionAuthenticatorHelper extends AuthenticatorHelper {

    public static final ChallengeScheme APIUS = 
        new ChallengeScheme(
                "HTTP_APIUS", 
                "APIUS", 
            "A custom challenge authentication scheme for when a session token is used as the authenticating credential.");
    
    /**
     * Constructor
     */
    public SessionAuthenticatorHelper() {
        super(APIUS, true, true);
    }
    
    @Override
    public void formatRawRequest(ChallengeWriter cw, 
                                 ChallengeRequest cr, 
                                 Response response, 
                                 Series<Parameter> httpHeaders) throws IOException {
        if (cr.getRealm() != null) {
            cw.appendQuotedChallengeParameter("realm", cr.getRealm());
        }
    }
    
    @Override
    public void parseResponse(ChallengeResponse cr, 
                              Request request, 
                              Series<Parameter> httpHeaders) {
        cr.setRawValue(cr.getRawValue().replaceFirst("token=", ""));
    }

}