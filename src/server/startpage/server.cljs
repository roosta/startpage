(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [startpage.handler :as handler]
            [figwheel.client :as fw]))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce serve-static (nodejs/require "serve-static"))
(defonce figlet (nodejs/require "figlet"))
(defonce body-parser (nodejs/require "body-parser"))
(defonce org (nodejs/require "org-mode-parser"))
(defonce http (nodejs/require "http"))
(defonce json-parser (.json body-parser))

;; app gets redefined on reload
(def app (express))

(defn handle-request [req res]
  (.send res (handler/render-page (.-path req))))

(defn handle-org
  [req res]
  (if-let [node-list (.makelist org "TODOs.org"
                             (fn [node-list]
                               (.send res node-list)))]
    (.sendStatus res 400)))

(defn handle-figlet
  [req res]
  (if-let [text (.. req -body -text)]
    (figlet text "DOS Rebel" (fn [err text]
                             (when err
                               (.log js/console "something went wrong")
                               (.dir js/console err))
                             (.send res text)))
    (.sendStatus res 400)))

;; routes get redefined on each reload
(.get app "/" handle-request)
(.get app "/org" handle-org)
(.post app "/figlet/" json-parser handle-figlet)
(.use app (serve-static "resources/public"))

(def -main
  (fn []
    ;; This is the secret sauce. you want to capture a reference to
    ;; the app function (don't use it directly) this allows it to be redefined on each reload
    ;; this allows you to change routes and have them hot loaded as you
    ;; code.
    (doto (.createServer http #(app %1 %2))
      (.listen 3000))))

(set! *main-cli-fn* -main)

(fw/start { })
