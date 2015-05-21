(defproject wind "0.1.0-SNAPSHOT"
  :description "Example of a clj webapp/notifier to find windy spots "
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure        "1.6.0"]
                 [octia                      "0.0.2"]
                 [ring/ring-jetty-adapter    "1.2.0"]
                 [ring/ring-core             "1.2.0" :exclusions [org.clojure/tools.reader]]
                 [cheshire                   "5.3.1"]
                 [clj-http                   "1.0.1"]
                 [me.shenfeng/mustache       "1.1"  ]
                 [org.slf4j/slf4j-api        "1.6.1"]
                 [org.slf4j/slf4j-log4j12    "1.6.1"]
                 [clj-time                   "0.8.0"]
                 [org.clojure/tools.logging  "0.2.6"]
                 [org.clojure/core.memoize   "0.5.6"]
                 [tron                       "0.5.3"]]
  :ring {:handler wind.core/routes
         :init    wind.core/run-ring}

  :main wind.core

  :octia {:routes wind.core/routes})
