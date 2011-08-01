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

package org.apius.server.identity.session.client.openam;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apius.messaging.atom.AtomMessageBuilder;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.SaxRepresentation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * <p>
 * Helper class to handle responses coming back from the <a href="forgerock.com/openam.html">
 * OpenAM</a> session provisioner, authenticator and authorizer application.
 * </p>
 * <p>
 * Methods are not publicized outside of the package since this class is designed solely to
 * help the <code>SessionProvisionerProxyImpl</code> class to consume and transform responses
 * from the <a href="forgerock.com/openam.html">OpenAM</a> session provisioner.
 * </p>
 * <p>Here is a sampling of the attributes response string that OpenAM sends back:
 * 
 * <code>
 * userdetails.token.id=AQIC5wM2LY4SfczIKuNd_hAtjrJgLaZmKCszk_Dsqh1QVT0.*AAJTSQACMDE.*
 * userdetails.role=id=Hello World Group,ou=group,dc=apius,dc=org
 * userdetails.role=id=Test Group,ou=group,dc=apius,dc=org
 * userdetails.attribute.name=uid
 * userdetails.attribute.value=pmorris
 * userdetails.attribute.name=mail
 * userdetails.attribute.value=pmorris@nmh.org
 * userdetails.attribute.name=userpassword
 * userdetails.attribute.value={SSHA}spRdGLaBTRTOJCUVNbUOmNWhQUj/qmE2uUHecA==
 * userdetails.attribute.name=sn
 * userdetails.attribute.value=Morris
 * userdetails.attribute.name=cn
 * userdetails.attribute.value=Paul Morris
 * userdetails.attribute.name=givenname
 * userdetails.attribute.value=Paul
 * userdetails.attribute.name=dn
 * userdetails.attribute.value=uid=pmorris,ou=people,dc=apius,dc=org
 * userdetails.attribute.name=objectclass
 * userdetails.attribute.value=organizationalPerson
 * userdetails.attribute.value=person
 * userdetails.attribute.value=inetOrgPerson
 * userdetails.attribute.value=top
 * </code>
 * </p>
 * 
 * @author Paul Morris
 * 
 */
final class ResponseHelper implements ApplicationContextAware {
    
    private AtomMessageBuilder atomMessageBuilder;
    private HashMap<String,List<String>> attributesMap;
    private DomRepresentation dom;
    private Document document;
    private ApplicationContext appCxt;
    
    ResponseHelper(AtomMessageBuilder atomMessageBuilder, 
                   HashMap<String,List<String>> attributesMap, 
                   DomRepresentation dom) {
        this.atomMessageBuilder = atomMessageBuilder;
        this.attributesMap = attributesMap;
        this.dom = dom;
    }
    
    /**
     * Here's what the string that OpenAM sends back looks like:
     * 
     * "boolean=true" || "boolean=false"
     *
     * @param responseString
     * @return boolean
     */
    boolean extractBooleanFromResponseString(String responseString) {
        responseString = responseString.replaceFirst("boolean=", "").replaceFirst("\n", "");
    	
    	return Boolean.parseBoolean(responseString);
    }
    
    /**
     * @param responseString
     * @return Entry
     * @throws IOException
     * @throws SAXException
     */
    SaxRepresentation writeSessionAttributesXml(String responseString) throws IOException, SAXException {
        Entry entry = atomMessageBuilder.createEntry();
        entry.setTitle(atomMessageBuilder.createText("OpenAM Session Attributes"));
        entry.setId(extractTokenFromResponseString(responseString));
        
        createContentDocument(responseString);
        entry.setContent(atomMessageBuilder.createContent(dom));
        
        return entry;
    }
    
    private String extractTokenFromResponseString(String responseString) {
        String[] lines = responseString.split("\n");
        String token = "";
        
        for (String line : lines) {
            if (line.contains("token.id")) {
                token = extractAttributeNameOrValueFromLine(line);
                break;
            }
        }
        
        return token;
    }
    
    private void createContentDocument(String responseString) throws IOException {
        createDocumentShell();
        populateAttributesMap(responseString);
        
        for (Map.Entry<String,List<String>> attribute : attributesMap.entrySet()) {
            Element roleElmt = null;
            Element attributeElmt = null;
            Element valueElmt = null;
            roleElmt = document.createElement("role");
            attributeElmt = document.createElement("attribute");
            
            if (attribute.getKey().contains("role:")) {
                roleElmt.setAttribute("id", attribute.getValue().get(0)); //Only one value to pick from.
                document.getElementsByTagName("roles").item(0).appendChild(roleElmt);
            } else {
                attributeElmt.setAttribute("name", attribute.getKey());
                
                for (String value : attribute.getValue()) {
                    valueElmt = document.createElement("value");
                    valueElmt.setTextContent(value);
                    attributeElmt.appendChild(valueElmt);
                }
                document.getElementsByTagName("attributes").item(0).appendChild(attributeElmt);
            }
        }  
    }
    
    private void createDocumentShell() throws IOException {
        document = dom.getDocument();
        Element root = document.createElement("session-attributes");
        Element roles = document.createElement("roles");
        Element attributes = document.createElement("attributes");
        document.appendChild(root);
        root.appendChild(roles);
        root.appendChild(attributes);
    }
    
    @SuppressWarnings("unchecked") //Compiler warns about the unchecked cast when getting 
                                   //the List bean from ApplicationContext.
    private void populateAttributesMap(String responseString) {
        String[] lines = responseString.split("\n");
        String key = null;
        List<String> values = null;
        
        for (String line : lines) {    
            if (line.contains("role")) {
                values = (List<String>) appCxt.getBean("openAmAttributeValuesList");
                key = "role:" + extractRoleValueFromLine(line); //Appending value will keep key unique.
                values.add(extractRoleValueFromLine(line));
                attributesMap.put(key, values);
            } else if (line.contains("attribute.name")) {
                values = (List<String>) appCxt.getBean("openAmAttributeValuesList");
                key = extractAttributeNameOrValueFromLine(line);
            } else if (line.contains("attribute.value")) {
                values.add(extractAttributeNameOrValueFromLine(line));
                attributesMap.put(key, values);
            }
        }
    }
    
    private String extractAttributeNameOrValueFromLine(String line) {
        String[] s = line.split("=");
    	
        return s[1];
    }
    
    private String extractRoleValueFromLine(String line) {
        String[] s = line.split("id=");
        
        return s[1];
    }
    
    /**
     * @param responseString
     * @return
     */
    String extractUsernameFromSessionAttributes(String responseString) {
        String[] splitBeforeUsername = responseString.split("uid\nuserdetails.attribute.value=");
        String[] splitAfterUsername = splitBeforeUsername[1].split("\n");
        
        return splitAfterUsername[0];
    }

    @Override
    public void setApplicationContext(ApplicationContext appCxt) throws BeansException {
        this.appCxt = appCxt;
    }
	
}
