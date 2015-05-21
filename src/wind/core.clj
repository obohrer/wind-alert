(ns wind.core
  (:require [wind.formatting              :as formatting]
            [wind.data                    :as data]
            [ring.adapter.jetty           :as jetty]
            [clojure.tools.logging        :as log])
  (:use octia.core)
  (:gen-class))

(def server (atom nil))
(declare stop)

(defn exception-wrapper
  [handler]
  (fn [rq]
    (try
      (handler rq)
      (catch Throwable t
        (log/error t "error")
        "Error"))))

(def routes
  (group "/"
         {:wrappers [exception-wrapper]}
         (GET ""
               {:wrappers []
                :doc
                {:description "wind homepage"
                 :name "wind homepage"}}
               {:as params}
               {:body (->> (data/current-alerts) formatting/alerts->html)})
         (GET "ping"
               {:wrappers []
                :doc
                {:description "ping // monitoring route"
                 :name "ping"}}
               {:as params}
               {:body "PONG"})
         (ANY "*"
              {:doc {:name "unknown-route" :description "unknown-route"}}
              {:as request}
              {:status 404})))

(defn bind
  [port]
  (log/infof "Starting server on port %d..." port)
  (when @server
    (stop))
  (reset! server (jetty/run-jetty routes
                                  (merge {:port port}
                                         {:join? false})))
  (log/info "Server started"))

(defn stop
  []
  (log/info "Stopping server...")
  (.stop @server)
  (reset! server nil)
  (log/info "Server stopped"))

(defn parse-port-arg
  [args]
  (try
    (java.lang.Integer/parseInt (first args))
  (catch Exception e
    nil)))

(defn -main
  [& args]
  (if-let [port (parse-port-arg args)]
    (do
      (data/start-retrieval)
      (bind port))
    (log/info "Should be called with port : java -jar wind.jar port")))
