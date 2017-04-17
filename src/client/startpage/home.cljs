(ns startpage.home
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [cljs-http.client :as http]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d]))

;; (defonce org (r/atom []))

(defonce timer (r/atom (js/Date.)))
(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.))
                       1000))

(defn reddit-feed
  []
  [:div "reddit feed here"])

(defn get-org!
  [ref]
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! ref (:body resp)))))

(defstyle org-styles
  [:.root {:color (-> colors :bright-white :hex)}]
  )

(defn org
  []
  (let [org-data (r/atom [])
        org-updater (js/setInterval
                     #(get-org! org-data)
                     300000)]
    (r/create-class
     {:component-will-mount #(get-org! org-data)
      :reagent-render
      (fn []
        [:div
         {:class (:root org-styles)}
         [:ul]
         (for [node @org-data]
           ^{:key (:headline node)}
           [:li (:headline node)]
           )
         ])})))

(defstyle clock-style
  [:.root {:font-size "10px"
           :width "450px"
           :color (-> colors :bright-white :hex)}]
  [:.clock {}])

(defn clock
  [ascii]
  [:div
   {:class (:root clock-style)}
   [:pre
    {:class (:clock clock-style)}
    ascii]])

(defn watcher-fn
  [_ _ _ new ref]
  (let [time-str (-> new .toTimeString (clojure.string/split " ") first)]
    (go
      (when-let [resp (<! (http/post "/figlet" {:json-params {:text time-str}}))]
        (reset! ref (:body resp))))))


(defstyle startpage-style
  [:.root {:color "white"
           :display "flex"
           :justify-content "space-around"
           :height "100vh"}])

(defn startpage
  []
  (let [ascii (r/atom "")]
    (r/create-class
     {:component-did-mount (fn []
                             (add-watch timer :watcher #(watcher-fn %1 %2 %3 %4 ascii)))
      :reagent-render
      (fn []
        [:div {:class (:root startpage-style)}
         [org]
         [clock @ascii]
         [reddit-feed]
         ])})))
