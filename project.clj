(defproject WebApiWrapper "1.0.0-SNAPSHOT"
  :description "Web API Wrapper for MCM"
  :dependencies [
    [org.clojure/clojure "1.4.0"]
    [net.java.dev.jets3t/jets3t "0.7.1"]
    [com.google.code.typica/typica "1.6"]
    [clj-ssh "0.4.0"]
    [com.amazonaws/aws-java-sdk "1.3.0"]]
  :dev-dependencies [
    [leiningen/lein-swank "1.1.0"]
    [lein-clojars "0.5.0-SNAPSHOT"]]
  :native-path "native" ; force forked jvm as a workaround
  :main WebApiWrapper
  :aot :all
)
