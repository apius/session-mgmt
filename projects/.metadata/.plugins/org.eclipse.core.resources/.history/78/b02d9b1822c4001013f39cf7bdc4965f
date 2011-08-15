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

/**
 * <p>
 * A set of constants that will be used to construct a <code>CookieSetting
 * </code> object (part of the <a href=www.restlet.org>Restlet</a> framework).
 * Constants were used since <code>CookieSetting</code> is a final class that 
 * cannot be extended.
 * </p>
 * <p>
 * A value for the <code>domain</code> parameter is not given here since that 
 * is best determined by calling <code>getOriginalRef().getHostDomain()</code> 
 * from within the context of the URI from where the cookie is being set.
 * </p>
 * <p>
 * A value for the <code>path</code> parameter is not given here since that 
 * is best determined by calling <code>getOriginalRef().getPath()</code> from
 * within the context of the URI from where the cookie is being set.
 * </p>
 *
 * @author Paul Morris
 * 
 */
public class SessionCookieSettings {
    
    /**
     * The <code>version</code> is set to 0 for cross-browser compatibility.
     */
    public static final int     version = 0;
    public static final String  name    = "APIUS";
    public static final String  comment = "APIUS, an API for the REST of us.";
    
    /**
     * Number of seconds.
     */
    public static final int     maxAge = 36000;
    
    /**
     * This cookie will contain a session token so, for security reasons, 
     * should be passed over SSL/TLS when in production. 
     */
    public static final boolean secure = true;
    
    /**
     * In a stateful session implementation of the <a href=www.apius.org>APIUS<a> 
     * framework this cookie is strictly used as a client side storage container 
     * (i.e. not to be passed back and forth along with every API call). Therefore, 
     * the <code>accessRestricted</code> parameter should be set to false when 
     * constructing the <code>CookieSetting</code> object so that the <a href=
     * www.apius.org>APIUS</a> client libraries can access the token value from 
     * client-side scripts.
     */
    public static final boolean accessRestricted = false;

}

