APIUS 0.2.3 (August 2011)
-------------------------------------
http://www.apius.org

1. INTRODUCTION
An API for the "REST" of us.

2. RELEASE NOTES
Identity service is fully functional with a common interface for managing sessions on remote
stateful session provisioner. Sessions can be created, refreshed, deleted (logged out) and 
attributes of the session can be retrieved. The first concrete implementation is for the OpenAM 
product and includes integration with the Atom Syndication protocol as well as filters for 
authenticating and authorizing sessions (users) against OpenAM policies before passing requests
through to protected resources.

3. DISTRIBUTION JAR FILES
org.apius.server.jar
org.apius.server.identity.jar

4. GETTING STARTED
