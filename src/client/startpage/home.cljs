(ns startpage.home
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [startpage.utils :refer [truncate-string join-classes]]
   [cljs-http.client :as http]
   [garden.units :as u :refer [px pt pc]]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(defn get-reddit!
  [ref]
  (go
    (let [resp (<! (http/get "/reddit"))]
      (reset! ref (as-> (:body resp) r
                    (.parse js/JSON r)
                    (gobj/getValueByKeys r "data" "children")
                    (take 30 r))))))

(defstyle reddit-style
  [:.root {:flex-basis "33.333333333%"
           :max-width "33.333333333%"}
   [:ul {:list-style-type "square"
         :padding 0
         :font-size "14px"}]]
  [:.header
   {:font-size "10px"
    :margin-left "-8px"}]
  )

(defn reddit
  []
  (let [reddit-data (r/atom {})
        reddit-updater (js/setInterval
                        #(get-reddit! reddit-data)
                        60000)
        header-text (r/atom "Reddit")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Reddit"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))
        ]
    (r/create-class
     {:component-will-unmount #(js/clearInterval reddit-updater)
      :component-will-mount #(get-reddit! reddit-data)
      :reagent-render
      (fn []
        [:div
         {:class (:root reddit-style)}
         [:pre {:class (:header reddit-style)}
          @header-text]
         [:ul
          (for [node @reddit-data]
            ^{:key (gobj/getValueByKeys node "data" "id")}
            (let [title (truncate-string (gobj/getValueByKeys node "data" "title") 50)
                  id (gobj/getValueByKeys node "data" "id")]
              [:li {:key id}
               title]))]])})))

(defn get-org!
  [ref]
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! ref (:body resp)))))

(defstyle org-styles
  [:.root {:flex-basis "33.333333333%"
           :max-width "33.333333333%"
           :text-align "right"}
   [:ul {:margin 0
         :font-size "14px"
         :direction "rtl"
         :padding 0
         :list-style-type "square"
         :list-style-position "outside"}]
   [:li {:cursor "pointer"
         :transition "background 100ms ease-in-out"}
    [:&:hover {:background (-> colors :bright-black :hex)}]]]
  [:.header
   {:font-size (px 10)
    }]
  [:.done {:color (-> colors :green :hex)}]
  [:.todo {:color (-> colors :yellow :hex)}]
  [:.someday {:color (-> colors :white :hex)}]
  [:.todo-node {:font-weight "bold"
                :margin-left "6px"}])

(defn org
  []
  (let [org-data (r/atom [])
        org-updater (js/setInterval
                     #(get-org! org-data)
                     60000)
        header-text (r/atom "Todo list")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Todo list"
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
          (map
           (fn [node]
             (when (= (:level node) 1)
               [:li
                {:key (:key node)
                 :on-click #(http/post "/org/open" {:json-params {:search-str (:headline node)}})}
                [:span (truncate-string (:headline node) 60)]
                [:span {:class (condp = (:todo node)
                                 "DONE" (join-classes org-styles :todo-node :done)
                                 "TODO" (join-classes org-styles :todo-node :todo)
                                 "SOMEDAY" (join-classes org-styles :todo-node :someday)
                                 "MAYBE" (join-classes org-styles :todo-node :someday)
                                 (:todo-node org-styles))}
                 (:todo node)]
                ]))
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
           :flex-basis "33.3333333%"
           :max-width "33.3333333%"
           :align-items "center"}
   [:pre {:margin 0}]
   [:img {:width "75%"
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
           :align-items "center"
           :height "100vh"
           :justify-content "space-between"
           :margin (px 30)
           }])

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      [:div {:class (:root startpage-style)}
       [reddit]
       [clock]
       [org]
       ])}))
