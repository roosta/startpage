(ns startpage.core
  (:require [reagent.core :as r]
            [reagent.debug :as d]
            [startpage.home :refer [startpage]]
            [secretary.core :as secretary :refer-macros [defroute]]))

(def current-page (r/atom nil))

(secretary/set-config! :prefix "/")

(defn app-view []
  [:div [@current-page]])

(defroute "/" []
  (reset! current-page startpage))

                                        ; the server side doesn't have history, so we want to make sure current-page is populated
(reset! current-page startpage)

(defn on-js-reload
  []
  (d/log "figwheel reloaded!"))
