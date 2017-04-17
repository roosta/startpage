(ns startpage.core
  (:import goog.History)
  (:require [reagent.core :as r]
            [reagent.debug :as d]
            [pushy.core :as pushy]
            [startpage.home :refer [startpage]]
            [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

(defonce current-page (r/atom nil))


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
