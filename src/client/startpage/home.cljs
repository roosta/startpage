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

(defn join-classes
  [styles & classes]
  (->> (select-keys styles classes)
       vals
       (clojure.string/join " ")))

(defstyle org-styles
  [:.root {}
   [:ul {:margin 0
         :font-size "14px"}]
   [:li {:cursor "pointer"
         :transition "background 100ms ease-in-out"}
    [:&:hover {:background (-> colors :bright-black :hex)}]]]
  [:.header
   {:font-size (px 10)
    :margin-bottom (px 10)}]
  [:.done {:color (-> colors :green :hex)}]
  [:.todo {:color (-> colors :yellow :hex)}]
  [:.someday {:color (-> colors :white :hex)}]
  [:.todo-node {:font-weight "bold"
           :margin-right "6px"}])

(defn org
  []
  (let [org-data (r/atom [])
        org-updater (js/setInterval
                     #(get-org! org-data)
                     60000)
        header-text (r/atom "Todo list")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Todo list:"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))]

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
          (map-indexed
           (fn [idx node]
             (when (= (:level node) 1)
               [:li
                {:key (:key node)
                 :on-click #(http/post "/org/open" {:json-params {:search-str (:headline node)}})}
                [:span {:class (condp = (:todo node)
                                 "DONE" (join-classes org-styles :todo-node :done)
                                 "TODO" (join-classes org-styles :todo-node :todo)
                                 "SOMEDAY" (join-classes org-styles :todo-node :someday)
                                 "MAYBE" (join-classes org-styles :todo-node :someday)
                                 (:todo-node org-styles))}
                 (:todo node)]
                [:span (:headline node)]]))
           @org-data)]])})))

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
       [reddit]])}))
