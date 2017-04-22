(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [startpage.handler :as handler]
            [clojure.string :as string]
            [goog.object :as gobj]
            [cljs.reader :refer [read-string]]
            [figwheel.client :as fw]
            [reagent.debug :as d]))

(nodejs/enable-util-print!)
(defonce chalk (nodejs/require "chalk"))

(defonce express (nodejs/require "express"))
(defonce serve-static (nodejs/require "serve-static"))
(defonce figlet (nodejs/require "figlet"))
(defonce body-parser (nodejs/require "body-parser"))
(defonce org (nodejs/require "org-mode-parser"))
(defonce http (nodejs/require "http"))
(defonce json-parser (.json body-parser))
(defonce fs (nodejs/require "fs"))
(defonce path (nodejs/require "path"))
(defonce child-process (nodejs/require "child_process"))

;; had issues using cljs-http so fell back to request
(defonce request (nodejs/require "request"))

;; app gets redefined on reload
(def app (express))

(defn handle-page-render
  "handle page render"
  [req res]
  (.send res (handler/render-page (.-path req))))

(def config (-> (.readFileSync fs "config.edn" "utf8")
                read-string))

(defn handle-org
  "return parsed org content"
  [req res]
  (if-let [node-list (.makelist org (:todo-file config)
                             (fn [node-list]
                               (.send res node-list)))]
    (.sendStatus res 400)))

(defn handle-figlet
  "send a figlet rendered font based on post params for font and text
  ie: {:json-params {:text \"text here\" :font \"figlet font\"}}"
  [req res]
  (let [text (gobj/getValueByKeys req "body" "text")
        font (gobj/getValueByKeys req "body" "font")]
    (if (not (and (nil? text) (nil? font)))
      (figlet text font (fn [err text]
                          (when err
                            (.log js/console "something went wrong")
                            (.dir js/console err))
                          (.send res (string/trimr text))))
      (.sendStatus res 400))))

(defn handle-emacs
  "open emacs at item line number by matching headline json param using grep
  Use child_process to call shell commands grep and emacsclient
  This function is hugely insecure, no sanitizing input and not tested for robustness
  USE AT OWN RISK"
  [req res]
  (let [search-str (gobj/getValueByKeys req "body" "search-str")]
    (.exec child-process
           (str "grep -n \"" search-str "\" " (:todo-file config))
           (fn [_ stdout _]
             (let [line-nr (first (clojure.string/split stdout #":"))]
               (.exec child-process
                      (str "emacsclient -n +" line-nr " " (:todo-file config))
                      (fn [err _ _]
                        (if err
                          (.sendStatus res 400)
                          (.sendStatus res 200)))))))))

(defn handle-reddit
  [req res]
  (let [opts (clj->js {:url (:reddit-feed-url config)
                       :headers {"User-Agent" "linux:sh.roosha.sh:v0.0.1 (by /u/zem_mattress)"}})]
    (request opts (fn [error resp body]
                    (.send res body)))))

;; ----- ROUTES -----
;; routes get redefined on each reload
(.get app "/" handle-page-render)
(.get app "/org" handle-org)
(.get app "/reddit" handle-reddit)
(.post app "/figlet/" json-parser handle-figlet)
(.use app (serve-static "resources/public"))
(.post app "/org/open/" json-parser handle-emacs)

(def -main
  (fn []
    ;; This is the secret sauce. you want to capture a reference to
    ;; the app function (don't use it directly) this allows it to be redefined on each reload
    ;; this allows you to change routes and have them hot loaded as you
    ;; code.
    (doto (.createServer http #(app %1 %2))
      (.listen 3000))))

(set! *main-cli-fn* -main)

(fw/start {:build-id "server"})
