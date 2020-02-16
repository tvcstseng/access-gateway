# access-gateway
Server that protect resources on appropriate access levels.

* Spring boot
* Spring security
* Spring mongo db
* Spring web services
* lombok


### Access level are broken up in LOW and HIGH
* Low is set to numeric password
* High is alpha numeric password


### To gain access do user logon
GET http://localhost:8080/access
Authorization header needs to be used to gain access.
input example Authorization: Basic "{requested_level}#{username}:{password}" encoded base64 


### Any static-resource available 
do a GET to retrieved all static resource relative paths from "http://localhost:8080/resources"

Resources can be requested directly "http://localhost:8080/level1/low_access.txt" 
If no appropriate session and csrf token is found access violation (403) or csrf violation (400) are thrown

Resources require session and appropriate access level to be requested 
Session are linked to session cookie GRANT

### Basic CSRF protection is all added
All authentication request will receive new csrf token, token are only readable from same domain and not modifiable. 
All request to protected resources require the CSRF token present

#### Example used for tests only:
GET  http://localhost:8080/level1/low_access.txt?XSRF=75DA5FAF2470BAA3_1581848921
XSRF token can be also be set as header for better security.
