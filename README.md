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

* Authorization header is for user access challenge.
* Input example Authorization: Basic "{requested_level}#{username}:{password}" encoded base64 
* On successful login sessions are granted to the browser

### Any static-resource available 

GET "http://localhost:8080/resources"

to retrieve list of all static resource relative paths 

#### Usage
* Resources can be accessed directly "http://localhost:8080/level1/low_access.txt" 
* Resources require session and appropriate access level to be requested 

#### Violations
If no appropriate session and csrf token is found access violation (403) or csrf violation (400) are thrown

#### Basic CSRF protection

* All successful authentication request will receive new csrf token, token are only readable from same domain and not modifiable. 
* All request to protected resources require the CSRF token present

#### Example used for tests only

GET  http://localhost:8080/level1/low_access.txt?XSRF=75DA5FAF2470BAA3_1581848921

XSRF token can be also be set as header for better security.
