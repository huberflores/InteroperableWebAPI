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


How to cite
-----------
If you are using the tool for your research, please do not forget to cite. Thanks!

- Flores, Huber, and Satish Srirama. ["Mobile Cloud Middleware"](http://www.sciencedirect.com/science/article/pii/S0164121213002318) Journal of Software and Systems, Volume 92, June 2014, Pages 82–94.


