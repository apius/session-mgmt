package org.apius.server.identity.session.impl.openam;

import java.io.IOException;

import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * <p>
 * Helper class to handle responses coming back from the <a href="forgerock.com/openam.html">
 * OpenAM</a> session provisioner, authenticator and authorizer application.
 * </p>
 * <p>
 * Methods are not publicized outside of the package since this class is designed solely 
 * to help the <code>SessionProvisionerProxyImpl</code> class to consume responses from the 
 * <a href="forgerock.com/openam.html">OpenAM</a> session provisioner.
 * </p>
 * 
 * @author Paul Morris
 * 
 */
public final class ResponseHelper {
    
    private DomRepresentation dom;
    
    boolean extractBooleanFromResponseString(String responseString) {
        responseString = responseString.replaceFirst("boolean=", "").replaceFirst("\n", "");
    	
    	return Boolean.parseBoolean(responseString);
    }
    
    DomRepresentation convertSessionAttributesToXml(String responseString) throws IOException {
        Document document = dom.getDocument();
    	Element root = document.createElement("session-attributes");
    	document.appendChild(root);
    	
    	Element sessionAttribute = null;
    	
    	String[] lines = responseString.split("\n");
    	
    	for (String line : lines) {	
    	    if (line.contains("attribute.name")) {
    	        sessionAttribute = document.createElement("attribute");
    	        sessionAttribute.setAttribute("name", extractAttributeNameOrValueFromLine(line));
    	        document.getFirstChild().appendChild(sessionAttribute);
    	    } else if (line.contains("attribute.value")) {
    	        Element value = document.createElement("value");
    	        sessionAttribute.appendChild(value);
    	        value.setTextContent(extractAttributeNameOrValueFromLine(line));
    	    }
    	}
    	
    	return dom;
    }
    
    private String extractAttributeNameOrValueFromLine(String line) {
        String[] s = line.split("=");
    	
        return s[1];
    }
    
    String extractUsernameFromSessionAttributes(String responseString) {
        String[] splitBeforeUsername = responseString.split("uid\nuserdetails.attribute.value=");
        String[] splitAfterUsername = splitBeforeUsername[1].split("\n");
        
        return splitAfterUsername[0];
    }
    
    public void setDom(DomRepresentation dom) {
        this.dom = dom;
    }
	
}
