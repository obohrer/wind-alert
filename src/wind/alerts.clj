(ns wind.alerts
  (:require [wind.config           :as config]
            [clj-http.client       :as http]
            [cheshire.core         :as cheshire]
            [clj-time.format       :as form]
            [clj-time.core         :as time]
            [clj-time.coerce       :as coerce]
            [clojure.string        :as string]
            [clojure.core.memoize  :as memo]
            [clojure.tools.logging :as log]))

(def base-url "http://api.windfinder.com/v2/spots/")

(defn gen-spot-url
  [{:keys [name id]}]
  (str base-url id "/reports/"))

(defn ts
  []
  (.getTime (java.util.Date.)))

(defn parse-json
  [x]
  (cheshire/parse-string x true))

(defn read-date
  [s]
  (form/parse (:basic-date-time-no-ms form/formatters) (string/replace s #"-|:" "")))

(def custom-formatter (form/formatter "yyyy-MM-dd"))

(defn write-date
  [d]
  (form/unparse custom-formatter d))

(defn get-token-rq
  []
  (log/info "getting new api token")
  (->> "http://www.windfinder.com/report/"
       http/get
       :body
       (re-find #"window.API_TOKEN = '([a-z0-9]{32})'")
       last))

(def get-token (memo/ttl get-token-rq {} :ttl/threshold (* 1000 60 30)))

(defn retrieve-observations
  [spot]
  (let [url (gen-spot-url spot)
        token (get-token)
        _ (log/info "will request observations observations for " spot "with token" token)
        rq (http/get url {:query-params {"token" token
                                         "limit" "-1"
                                         "timespan" "PT-24H"
                                         "step" "1m"
                                         "customer" "wfweb"
                                         "version" "1.0"
                                         "_" (ts)}})
        ws (->> rq
                :body
                parse-json
                (mapv #(vector (-> % :dtl_s read-date) (:ws %))))]
    (assoc spot :wind ws)))

(def not-empty? (complement empty?))

(defn alert?
  [date-threshold speed-threshold {:keys [wind] :as observation}]
  (->> wind
       (filter (comp #(when % (time/before? date-threshold %)) first))
       (filter (comp (partial <= speed-threshold) second))
       not-empty?))

(defn wait
  [_]
  (Thread/sleep 5000))

(defn fetch-current-alerts
  []
  (let [observations (->> (config/get :spots)
                          (map (juxt retrieve-observations wait)); To avoid hitting rate-limiting
                          (map first))
        date-threshold (time/minus (time/now) (time/hours 3))
        speed-threshold 7]; in knots
    (->> observations
         (filter (partial alert? date-threshold speed-threshold))
         (doall))))
