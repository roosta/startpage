(ns startpage.handler
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [secretary.core :as secretary]))

(enable-console-print!)

(defn template
  []
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Inconsolata" :rel "stylesheet"}]]
   [:body
    [:div#app]
    [:script {:type "text/javascript" :src "js/client.js"}]]])

(defn ^:export render-page [path]
  (rs/render-to-static-markup (do (secretary/dispatch! path)
                                  [template])))
