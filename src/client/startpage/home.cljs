(ns startpage.home
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [cljs-http.client :as http]
   [garden.units :as u :refer [px pt pc]]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(defstyle reddit-style
  [:.root {:margin 0}])

(defn reddit
  []
  [:div
   {:class (:root reddit-style)}
   "reddit feed here"])

(defn get-org!
  [ref]
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! ref (:body resp)))))

(defstyle org-styles
  [:.root {}
   [:ul {:padding-left (px 15)
         :margin 0}]]
  [:.header
   {:font-size (px 10)
    :margin-bottom (px 10)}
   ]
  )

(defn org
  []
  (let [org-data (r/atom [])
        org-updater (js/setInterval
                     #(get-org! org-data)
                     300000)
        header-text (r/atom "Todo list")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Todo list:"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))
        ]
    (r/create-class
     {:component-will-mount #(get-org! org-data)
      :component-will-unmount #(js/clearInterval org-updater)
      :reagent-render
      (fn []
        [:div
         {:class (:root org-styles)}
         [:pre {:class (:header org-styles)}
          @header-text]
         [:ul
          (for [node @org-data]
            ^{:key (:headline node)}
            [:li (:headline node)])]])})))

(defn get-figlet!
  [ref]
  (go
    (let [time-str (-> (js/Date.) .toTimeString (clojure.string/split " ") first)
          resp (<! (http/post "/figlet" {:json-params {:text time-str
                                                       :font "DOS Rebel"}}))]
      (reset! ref (:body resp)))))

(defstyle clock-style
  [:.root {:font-size (px 10)
           :display "flex"
           :flex-direction "column"
           :align-items "center"}
   [:pre {:margin 0}]
   [:img {:width "50%"
          :margin-bottom (px 20)}]])

(defn clock
  []
  (let [ascii (r/atom "")
        time-updater (js/setInterval
                      #(get-figlet! ascii)
                      1000)]
    (r/create-class
     {:component-will-mount #(get-figlet! ascii)
      :component-will-unmount #(js/clearInterval time-updater)
      :reagent-render
      (fn []
        [:div
         {:class (:root clock-style)}
         [:img {:src "/img/paxel.png"}]
         [:div {:class (:row clock-style)}
          [:pre
           {:class (:clock clock-style)}
           @ascii]]
         ])})))

(defstyle startpage-style
  [:.root {:color (-> colors :bright-white :hex)
           :display "flex"
           :align-items "flex-end"
           :justify-content "space-around"
           :margin (px 30)
           }])

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      [:div {:class (:root startpage-style)}
       [org]
       [clock]
       [reddit]
       ])}))
