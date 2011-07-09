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

import org.restlet.Component;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * <p>
 * If this application is run outside of a J2EE web container as a JSE application
 * then we need a way to point the application to our bean factory resource and 
 * start the component manually. 
 * </p>
 * Note that the <code>server</code> bean in the <code>identity-context.xml</code>
 * bean factory needs to be commented out so that the component "knows" to create
 * an HTTP server via the Restlet framework. Otherwise when running in a J2EE 
 * container, the container's HTTP connector is automatically chosen. Thus the
 * need to once again comment out the line doing the <code>server</code> injection
 * in the <code>identity-context.xml</code> file when running in a J2EE container.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public class JseBootstrap {
	
    public static void main(String[] args) throws Exception {
        ClassPathResource resource = new ClassPathResource("identity-context-jse.xml");
        BeanFactory beanFactory = new XmlBeanFactory(resource);
        
        Component component = (Component) beanFactory.getBean("component");
        component.start();
    }
}
