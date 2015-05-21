(ns wind.config
  (:require [cheshire.core   :as cheshire]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [get]))

(def cfg (atom nil))

(defn- read-cfg
  []
  (-> "wind.json"
      io/resource
      slurp
      (cheshire/parse-string true)))

(defn get
  [& ks]
  (-> cfg
      (swap! #(or % (read-cfg)))
      (get-in ks)))
