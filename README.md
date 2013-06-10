Interoperable Web API
===================

Interoperable Web API for Mobile Cloud Middleware. The Wrapper abstracts multiple Web APIs from different clouds in a common operation level. Moreover, it provides a unique interface for service invocation.

This wrapper includes the following Web APIs
- typica

- jetS3t

- Amazon API for S3

- GData 


Requirements
------------

- Leiningen (lein)


Installation
-------------

$ lein deps


$ lein uberjar

.
.
.
WebApiWrapper-1.0.0-SNAPSHOT-standalone.jar is created


Usage
------

- Modify the file cloud.properties with your cloud specifications

- Locate the Web API within the classpath of your java project

- Invoke the service using the unique interface <mcminteroperability> ([Gist example](https://gist.github.com/huberflores/5747779)) 


