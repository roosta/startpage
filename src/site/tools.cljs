(ns site.tools
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [secretary.core :as secretary]
            [startpage.core :as core]))

(enable-console-print!)

(defn template [{:keys [body]}]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1.0"}]]
   [:body
    [:div#app [body]]
    [:script {:type "text/javascript" :src "goog/base.js"}]
    [:script {:type "text/javascript" :src "//cdnjs.cloudflare.com/ajax/libs/fetch/1.0.0/fetch.min.js"}]
    [:script {:type "text/javascript" :src "app.js"}]
    [:script {:type                    "text/javascript"
              :dangerouslySetInnerHTML {:__html "goog.require('startpage.client');"}}]]])

(defn ^:export render-page [path]
  (rs/render-to-static-markup (do (secretary/dispatch! path)
                                  [template {:body core/app-view}])))
