# access-gateway

very simple access-gateway that regulates resources by access level. Users can only access resources once their access level is elevated to appropriate level.

### Tech stack
* Spring Boot Maven Plugin Reference Guide
* Spring Configuration Processor
* Spring Web
* Spring Web Services
* Apache Freemarker
* Spring Security
* Spring Data MongoDB


### Access levels Supported

* Low is set to numeric password
* High is alpha numeric password


### Access resource

GET "http://localhost:8080/access"

Authorization header required

Input example Authorization: Basic "{requested_level}#{username}:{password}" encoded base64 

Response

```
{"authenticated":true}
```

#### on success
* session cookie
* CSRF cookie

### Hosted static-resources available 

GET "http://localhost:8080/resources"

Response

```
{ 
   "resources":[ 
      "/Level1/low/access.txt",
      "/Level1/low_access.txt",
      "/Level2/high_access.txt",
      "/Level2/what/am/I/access.txt",
      "/css/main.css",
      "/js/main.js"
   ]
}
```

will give a list of all static resources hosted by the server 

#### Usage
* Resources can be accessed directly "http://localhost:8080/level1/low_access.txt" 
* Resources require session and appropriate access level to be requested 

#### Violations
If no appropriate session and csrf token is received on resource request, an access(403)/csrf(400) violation is returned.

```
{"requiredAccess":"Level1","message":"invalid access level"}
```

#### Basic CSRF protection

* All successful authentication request will receive new csrf token, tokens are only readable from same domain and not modifiable. 
* All request to protected resources require the CSRF token present

#### Example used for tests only

GET "http://localhost:8080/level1/low_access.txt?XSRF=75DA5FAF2470BAA3_1581848921"

XSRF token should always be used with the header option for passing the csrf cookie token value to server, example above is just for simple browser tests.
