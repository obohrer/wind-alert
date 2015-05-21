(ns wind.data
  (:require [wind.alerts           :as alerts]
            [wind.mailgun          :as mailgun]
            [wind.config           :as config]
            [tron                  :as tron]
            [clojure.tools.logging :as log]))

(def observations (atom []))

(def last-sending (atom nil))

(defn current-alerts
  []
  @observations)

(defn now-ts
  []
  (.getTime (java.util.Date.)))

(defn new-data
  [all-alerts alerts]
  ; Send alerts when a new spot is windy or every 3 hours
  (when (and (config/get :mail :enabled)
         (or (not-empty alerts)
            (and (not-empty all-alerts)
                 @last-sending
                 (> (- (now-ts) @last-sending)
                    (* 1000 60 60 3)))))
    (let [sending-result (mailgun/send-alerts! all-alerts)]
      (reset! last-sending (now-ts))
      (log/info "notification sent?" sending-result))))

(defn retrieve-store-data!
  []
  (try
    (let [old-obs    @observations
          already-on (->> old-obs (map :id) set)
          obs        (alerts/fetch-current-alerts)
          new-alerts (remove (comp already-on :id) obs)]
      (reset! observations obs)
      (new-data obs new-alerts))
  (catch Throwable t
    (log/error t "Can't retrieve observations data"))))

(defn start-retrieval
  []
  (tron/periodically :data-retrieval retrieve-store-data! 0 (* 1000 60 10)))
