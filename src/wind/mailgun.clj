(ns wind.mailgun
  (:require [wind.config           :as config]
            [clj-http.client       :as http]
            [clj-time.format       :as form]
            [clojure.string        :as string]
            [clojure.tools.logging :as log]))


(defn format-date
  [d]
  (form/unparse (form/formatter "HH:m") d))

(defn alert->msg
  [{:keys [name wind]}]
  (str name
       " : "
       (->> wind (take-last 3) (map second) (string/join " "))
       " [" (-> wind last first format-date) "]"))

(defn send-alerts!
  [alerts]
  (let [subject (str "it's windy at " (count alerts) " locations")]
    (->> {:basic-auth ["api" (config/get :mail :api-key)]
          :query-params {:from (config/get :mail :from)
                         :to (string/join "," (config/get :recipients))
                         :subject subject
                         :text (str (->> alerts (map alert->msg) (string/join "\n"))
                                    "\nhttp://wind.oliverro.com")}}
         (http/post :mail)
         :status
         (= 200))))
