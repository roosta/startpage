(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [site.tools :as tools]))

(nodejs/enable-util-print!)

(def express (nodejs/require "express"))
(def serve-static (nodejs/require "serve-static"))
(def figlet (nodejs/require "figlet"))
(def body-parser (nodejs/require "body-parser"))
;; (def org (nodejs/require "org"))
;; (def fs (nodejs/require "fs"))
(def org (nodejs/require "org-mode-parser"))

(defn handle-request [req res]
  (.send res (tools/render-page (.-path req))))

(defn handle-org
  [req res]
  (if-let [node-list (.makelist org "README.org"
                             (fn [node-list]
                               (.send res node-list)))]
    (.sendStatus res 400)))

#_(defn handle-org
  [req res]
  (let [parser (org.Parser.)
        org-document (.parse parser "* hello world")
        html-document (.convert org-document (.-ConverterHTML org) #js {:headerOffset 1
                                                                        :exportFromLineNumber false
                                                                        :suppressSubScriptHandling false
                                                                        :suppressAutoLink false})]
    (.send res html-document)
    ))

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
    (.get app "/org" handle-org)
    (.post app "/figlet/" json-parser handle-figlet)
    (.use app (serve-static "resources/public/js"))
    (.listen app 3000 (fn []
                        (println "Server started on port 3000")))))

(set! *main-cli-fn* -main)
