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

(defonce org (r/atom []))
(defn get-org!
  []
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! org (:body resp)))))

(defonce timer (r/atom (js/Date.)))
(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.))
                       1000))
(defonce org-updater (js/setInterval
                      #(get-org!)
                      300000))

(defn reddit-feed
  []
  [:div "reddit feed here"])

(defstyle org-styles
  [:.root {:color (-> colors :bright-white :hex)}]
  )

(defn org-component
  [data]
  [:div
   {:class (:root org-styles)}
   [:ul]
   (for [node data]
     ^{:key (:headline node)}
     [:li (:headline node)]
     )
   ])

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
                             (get-org!)
                             (add-watch timer :watcher #(watcher-fn %1 %2 %3 %4 ascii)))
      :reagent-render
      (fn []
        [:div {:class (:root startpage-style)}
         [org-component @org]
         [clock @ascii]
         [reddit-feed]
         ])})))

#_(def startpage (r/adapt-react-class (style-wrapper (startpage*))))
