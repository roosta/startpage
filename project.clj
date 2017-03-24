(defproject startpage "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.473"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds [{:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main "startpage.core"
                                   :output-to "resources/public/js/app.js"
                                   :verbose true
                                   :pretty-print false
                                   :optimizations :advanced
                                   :closure-defines {"goog.DEBUG" false}}}]}

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [figwheel-sidecar "0.5.9"]
                                  [binaryage/devtools "0.9.1"]]
                   :plugins [[lein-figwheel "0.5.9"]]
                   :source-paths ["dev" "script"]}})
