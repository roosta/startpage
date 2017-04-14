(ns startpage.server
  (:require [cljs.nodejs :as nodejs]
            [reagent.core :as r]
            [reagent.dom.server :as rs]))

(nodejs/enable-util-print!)

(def express (nodejs/require "express"))

(defn template []
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1.0"}]]
   [:body
    [:div#app
     [:h1 "Server Rendering!!!"]]]])

(defn ^:export render-page [path]
  (rs/render-to-static-markup [template]))

(defn handle-request [req res]
  (.send res (render-page (.-path req))))

(defn -main []
  (let [app (express)]
    (.get app "/" handle-request)
    (.listen app 3000 (fn []
                        (println "Server started on port 3000")))))

(set! *main-cli-fn* -main)
