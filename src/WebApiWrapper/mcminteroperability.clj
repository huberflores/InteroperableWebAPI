;@author
;Huber Flores, 2012

(ns WebApiWrapper.mcminteroperability
  (:use [WebApiWrapper.ec2]
        [WebApiWrapper.s3])
  (:gen-class
    :name WebApiWrapper.mcminteroperability
    :methods [#^{:static true} [startInstance [String String String] String]
     #^{:static true} [awsS3 [String String String String] com.amazonaws.services.s3.model.PutObjectResult]]
  ))

(require '[WebApiWrapper.ec2 :as ec2])
(require '[WebApiWrapper.natives3 :as natives3])
(require '[WebApiWrapper.s3 :as jetapis3])

;These are comments
;(def cred {:access-key "your-idkey", :secret-key "your-secretkey"})
;example
;(WebApiWrapper.mcminteroperability/startInstance "AccessKEY" "SecretKey" "ami-96b74eff")

(defn startInstance
  "Received the credentials and the image identifier." 
  [aws-id aws-key image-id]
  	 
  (ec2/run-instance aws-id aws-key image-id))

(defn -startInstance
  "A Java-callable wrapper around the 'startInstance' function."
  [aws-id aws-key image-id]
  (startInstance aws-id aws-key image-id))   

(defn awsS3
  "Received the file." 
  [aws-id aws-key pathfile bucket]
    
  (let [file (clojure.java.io/file pathfile)] 
	 
  (natives3/put-object {:access-key aws-id, :secret-key aws-key} bucket (.getName file)  file)))

(defn -awsS3
  "A Java-callable wrapper around the 'awsS3' function."
  [aws-id aws-key pathfile bucket]
  (awsS3 aws-id aws-key pathfile bucket))   





