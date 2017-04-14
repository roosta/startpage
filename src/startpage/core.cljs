(ns startpage.core
  (:require [reagent.core :refer [atom]]
            [secretary.core :as secretary :refer-macros [defroute]]))

(def current-page (atom nil))

(defn home-page []
  [:div [:h1 "Home Page"]])

(defn app-view []
  [:div [@current-page]])

(secretary/set-config! :prefix "/")

(defroute "/" []
  (.log js/console "home page")
  (reset! current-page home-page))

; the server side doesn't have history, so we want to make sure current-page is populated
(reset! current-page home-page)
