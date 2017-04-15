(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [site.tools :as tools]))

(nodejs/enable-util-print!)

(def express (nodejs/require "express"))
(def serve-static (nodejs/require "serve-static"))
(def figlet (nodejs/require "figlet"))
(def body-parser (nodejs/require "body-parser"))

(defn handle-request [req res]
  (.send res (tools/render-page (.-path req))))

(defn handle-figlet
  [req res]
  (if-let [text (.. req -body -text)]
    (figlet text "Fraktur" (fn [err text]
                             (when err
                               (.log js/console "something went wrong")
                               (.dir js/console err))
                             (.send res text)))
    (.sendStatus res 400)))

(defn -main []
  (let [app (express)
        json-parser (.json body-parser)]
    (.get app "/" handle-request)
    (.post app "/figlet/" json-parser handle-figlet)
    (.use app (serve-static "resources/public/js"))
    (.listen app 3000 (fn []
                        (println "Server started on port 3000")))))

(set! *main-cli-fn* -main)
