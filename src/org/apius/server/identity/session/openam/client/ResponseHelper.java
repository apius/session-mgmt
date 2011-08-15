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

package org.apius.server.identity.session.openam.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Text;
import org.restlet.representation.StringRepresentation;

/**
 * 
 * <p>
 * Helper class to handle responses coming back from the <a href="forgerock.com/openam.html">
 * OpenAM</a> session provisioner, authenticator and authorizer application.
 * </p>
 * <p>
 * Methods are not publicized outside of the package since this class is designed solely to
 * help the <code>SessionProvisionerProxy</code> class to consume and transform responses
 * from the <a href="forgerock.com/openam.html">OpenAM</a> session provisioner.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
final class ResponseHelper {
		
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
     * <p>
     * Here is a sampling of the attributes response string that OpenAM sends back:
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
     * @param responseString
     * @return attributes and roles as an Atom Feed
     */
	Feed writeSessionAttributesFeed(String responseString) {
    	Feed feed = new Feed();
    	feed.setId(extractTokenFromResponseString(responseString));
    	feed.setTitle(new Text("OpenAM Session Attributes"));
    	
    	HashMap<String, List<String>> attributes = populateAttributesMap(responseString);
		
		for (Map.Entry<String,List<String>> attribute : attributes.entrySet()) {
			Entry entry = new Entry();
			Content content = new Content();
			
			entry.setTitle(new Text(attribute.getKey()));			
			content.setInlineContent(buildMultipleValuesContent(attribute.getValue()));
			entry.setContent(content);
			feed.getEntries().add(entry);
		}
        
        return feed;
    }
    
	
    private String extractTokenFromResponseString(String responseString) {
        String[] lines = responseString.split("\n");
        String token = "";
        
        for (String line : lines) {
            if (line.contains("token.id")) {
                token = extractTokenFromLine(line);
                break;
            }
        }
        
        return token;
    }
    
    private HashMap<String, List<String>> populateAttributesMap(String responseString) {
    	HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
    	List<String> roles = new ArrayList<String>();
    	List<String> attributeValues = new ArrayList<String>();
    	
    	String[] lines = responseString.split("\n");
    	String currentKey = "";
    	
		for (String line : lines) {
			if (line.contains("role")) {
				roles.add(extractRoleValueFromLine(line));
			} else if (line.contains("attribute.name")) {
				if (!attributeValues.isEmpty()) {
					attributes.put(currentKey, attributeValues);
					attributeValues = new ArrayList<String>();
				}
				currentKey = extractAttributeNameFromLine(line);
			} else if (line.contains("attribute.value")) {
				attributeValues.add(extractAttributeValueFromLine(line));
			}
        }
		attributes.put(currentKey, attributeValues);
		attributes.put("roles", roles);
		
		return attributes;
    }
    
    private String extractTokenFromLine(String line) {
        String[] s = line.split("=");
    	
        return s[1];
    }
    
    private String extractAttributeNameFromLine(String line) {
        String[] s = line.split("name=");
    	
        return s[1];
    }
    
    private String extractAttributeValueFromLine(String line) {
        String[] s = line.split("value=");
    	
        return s[1];
    }
    
    private String extractRoleValueFromLine(String line) {
        String[] s = line.split("id=");
        
        return s[1];
    }
    
    private StringRepresentation buildMultipleValuesContent(List<String> values) {
    	StringBuilder sb = new StringBuilder();
    	
    	for (String value : values) {
			sb.append(value + ";");
		}
    	
    	return new StringRepresentation(sb.toString().substring(0, sb.length()-1));
    }
    
    /**
     * @param responseString
     * @return username
     */
    String extractUsernameFromSessionAttributes(String responseString) {
        String[] splitBeforeUsername = responseString.split("uid\nuserdetails.attribute.value=");
        String[] splitAfterUsername = splitBeforeUsername[1].split("\n");
        
        return splitAfterUsername[0];
    }
	
}