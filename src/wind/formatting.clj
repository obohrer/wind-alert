(ns wind.formatting
  (:require [me.shenfeng.mustache  :as mustache]
            [clj-time.format       :as form]
            [clj-time.core         :as clj-time]
            [clojure.java.io       :as io]
            [cheshire.core         :as cheshire]
            [clojure.tools.logging :as log]))

(def main-tpl (delay (-> "templates/main.html" io/resource slurp mustache/mk-template)))
(def alert-tpl (delay (-> "templates/alert.html" io/resource slurp)))
(def chart-tpl (delay (-> "templates/chart.html" io/resource slurp)))

(defn format-long-date
  [d]
  (form/unparse (form/formatter "dd/MM/yyyy HH:mm") d))

(defn format-short-date
  [d]
  (form/unparse (form/formatter "HH:mm") d))

(defn add-json-chart
  [{:keys [wind] :as alert}]
  (let [wind (take-last 12 wind)
        labels (->> wind (map (comp format-short-date first)))
        data   (map second wind)]
    (assoc alert :labels-json (cheshire/generate-string labels)
                 :data-json (cheshire/generate-string data))))

(defn format-alert
  [{:keys [wind] :as alert}]
  (-> alert
      add-json-chart
      (assoc :current (-> wind last last))))

(defn format-alerts
  [alerts]
  (mapv format-alert alerts))

(defn alerts->html
  [alerts]
  (mustache/to-html @main-tpl
                    {:time (-> (clj-time/now) format-long-date)
                     :alerts (format-alerts alerts)}
                    {:alert @alert-tpl
                     :chart @chart-tpl}))
