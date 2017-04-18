(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [startpage.handler :as handler]
            [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
            [clojure.string :as string]
            [goog.object :as gobj]
            [cljs.reader :refer [read-string]]
            [figwheel.client :as fw]
            [reagent.debug :as d])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(nodejs/enable-util-print!)

(defonce express (nodejs/require "express"))
(defonce serve-static (nodejs/require "serve-static"))
(defonce figlet (nodejs/require "figlet"))
(defonce body-parser (nodejs/require "body-parser"))
(defonce org (nodejs/require "org-mode-parser"))
(defonce http (nodejs/require "http"))
(defonce json-parser (.json body-parser))
(defonce fs (nodejs/require "fs"))
(defonce path (nodejs/require "path"))
(defonce colors (nodejs/require "colors"))
(defonce feedparser (nodejs/require "feedparser"))
(defonce cp (nodejs/require "child_process"))

;; (.exec cp "ls" #(d/log %2))

;; app gets redefined on reload
(def app (express))

(defn handle-request [req res]
  (.send res (handler/render-page (.-path req))))

(def config (-> (.readFileSync fs "config.edn" "utf8")
                read-string))

(defn handle-org
  [req res]
  (if-let [node-list (.makelist org (:todo-file config)
                             (fn [node-list]
                               (.send res node-list)))]
    (.sendStatus res 400)))

(defn handle-figlet
  [req res]
  (let [text (.. req -body -text)
        font (.. req -body -font)]
    (if (not (and (nil? text) (nil? font)))
      (figlet text font (fn [err text]
                          (when err
                            (.log js/console "something went wrong")
                            (.dir js/console err))
                          (.send res (string/trimr text))))
      (.sendStatus res 400))))

(defn handle-emacs
  [req res]
  (if-let [linenr (gobj/getValueByKeys req "params" "linenr")]
    (.exec cp
           (str "emacsclient -n +" linenr " " (:todo-file config))
           #(if (not (nil? %1))
              (do
                (.sendStatus res 400)
                (.send res %1))
              (.sendStatus res 200)))
    (.sendStatus res 400)))

;; routes get redefined on each reload
(.get app "/" handle-request)
(.get app "/org" handle-org)
(.post app "/figlet/" json-parser handle-figlet)
(.use app (serve-static "resources/public"))
(.get app "/org/open/:linenr" handle-emacs)

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
