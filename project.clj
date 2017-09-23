(defproject startpage "1.0.0"
  :description "Startpage server/client that displays a reddit feed and an org-mode todo file"
  :url "startpage.roosta.sh"
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

  :figwheel {:open-file-command "script/emacs-file-opener.sh"
             :css-dirs ["resources/public/css"]
             :server-logfile "log/fighweel-server.log"}

  :aliases {"min" ["do"
                   ["clean"]
                   ["cljsbuild" "once" "server-min" "client-min"]]
            "demo" ["do"
                    ["clean"]
                    ["cljsbuild" "once" "server-demo" "client-min"]]}

  :cljsbuild {:builds [{:id "client"
                        :source-paths ["src/client"]
                        :figwheel true
                        :compiler {:main startpage.core
                                   :asset-path "/js/client"
                                   :output-to "resources/public/js/client.js"
                                   :output-dir "resources/public/js/client"
                                   :optimizations :none
                                   :preloads [devtools.preload]
                                   :source-map true}}

                       {:id "client-min"
                        :source-paths ["src/client"]
                        :compiler {:main startpage.core
                                   :output-to "resources/public/js/client.js"
                                   :pretty-print false
                                   :optimizations :advanced}}

                       {:id "server"
                        :source-paths ["src/server"]
                        :compiler {:main startpage.server
                                   :output-to "target/dev/server.js"
                                   :output-dir "target/dev/server"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true}}

                       {:id "server-min"
                        :source-paths ["src/server"]
                        :compiler {:main startpage.server
                                   :output-to "target/prod/server.js"
                                   :output-dir "target/prod/server"
                                   :closure-defines {startpage.server/DEBUG false}
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map false}}

                       {:id "server-demo"
                        :source-paths ["src/server"]
                        :compiler {:main startpage.server
                                   :output-to "target/demo/server.js"
                                   :output-dir "target/demo/server"
                                   :closure-defines {startpage.server/DEBUG false
                                                     startpage.server/config-location "config.example.edn"}
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map false}}]})
