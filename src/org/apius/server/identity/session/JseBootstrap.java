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
 * then we need to point the application to our JSE-specific bean factory resource 
 * ("identity-context-jse.xml") and we need to start the component manually. 
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
