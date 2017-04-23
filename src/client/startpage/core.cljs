(ns startpage.core
  (:import goog.History)
  (:require [reagent.core :as r]
            [reagent.debug :as d]
            [figwheel.client :as fw]
            [pushy.core :as pushy]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [startpage.home :refer [startpage]]
            [startpage.srcery :refer [colors]]
            [secretary.core :as secretary :refer-macros [defroute]]))

(goog-define DEBUG true)
(when goog.DEBUG
  (enable-console-print!))

(defonce current-page (r/atom nil))

(defstyle core-style
  [:body {:margin 0
          :font-family "monospace"
          :background-color (-> colors :black :hex)}])

(defn appframe
  []
  [:div [@current-page]])

(defroute "/"
  []
  (reset! current-page startpage))

(defn on-js-reload
  []
  (reset! current-page startpage))

(defn mount-root
  []
  (r/render [appframe] (.getElementById js/document "app")))

(defn init!
  []

  (reset! current-page startpage)
  (secretary/set-config! :prefix "/")
  (pushy/push-state! secretary/dispatch!
                     (fn [x] (when (secretary/locate-route x) x)))
  (mount-root))

(init!)
