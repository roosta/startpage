(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [site.tools :as tools]))

(nodejs/enable-util-print!)

(def express (nodejs/require "express"))
(def serve-static (nodejs/require "serve-static"))
(def figlet (nodejs/require "figlet"))

(defn handle-request [req res]
  (.send res (tools/render-page (.-path req))))

(defn handle-figlet
  [req res]
  (let [input (.. req -params -input)]
    (figlet input "Fraktur" (fn [err text]
                              (when err
                                (.log js/console "something went wrong")
                                (.dir js/console err))
                              (.send res text)))))

(defn -main []
  (let [app (express)]
    (.get app "/" handle-request)
    (.get app "/figlet/:input" handle-figlet)
    (.use app (serve-static "resources/public/js"))
    (.listen app 3000 (fn []
                        (println "Server started on port 3000")))))

(set! *main-cli-fn* -main)
