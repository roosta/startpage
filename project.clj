(defproject startpage "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [org.clojure/clojurescript "1.9.521"]
                 [cljs-http "0.1.42"]
                 [reagent "0.6.1"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.3.442"]
                 [kibu/pushy "0.3.7"]
                 [cljs-css-modules "0.2.1"]]

  :plugins [[lein-cljsbuild "1.1.6-SNAPSHOT"]]

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]
                                  [binaryage/devtools "0.9.3"]
                                  [figwheel-sidecar "0.5.10"]]
                   :plugins [[lein-figwheel "0.5.10"]]
                   :source-paths ["dev" "script"]}}

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds {:app-min {:source-paths ["src/client"]
                                 :compiler {:output-to "resources/public/js/app.js"
                                            :output-dir "resources/public/js"
                                            :optimizations :advanced}}}})
