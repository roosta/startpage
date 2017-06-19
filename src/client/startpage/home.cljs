(ns startpage.home
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [startpage.utils :refer [truncate-string join-classes transition]]
   [cljs-http.client :as http]
   [garden.units :as u :refer [px pt pc]]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d]
   [figwheel.client.utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce appdb (r/atom {:org nil
                        :count nil
                        :reddit nil}))

(defn get-reddit!
  "get reddit front page feed, and take c"
  [c]
  (go
    (let [resp (<! (http/get "/reddit"))]
      (swap! appdb assoc :reddit (as-> (:body resp) r
                                    (.parse js/JSON r)
                                    (gobj/getValueByKeys r "data" "children")
                                    (take c r))))))

(defstyle reddit-style
  [:.root {:flex-basis "33.333333333%"
           :max-width "33.333333333%"}
   [:ul {:list-style-type "square"
         :padding 0
         :font-size "14px"}
    [:li {:position "relative"}
     [:&:hover {:background (-> colors :bright-black :hex)
                :font-weight "bold"}]]]
   [:a {:text-decoration "none"
        :color (-> colors :bright-white :hex)}
    [:&:visited {:color (-> colors :white :hex)}]]]

  [:.header
   {:font-size "10px"
    :margin-left "-8px"}])

(defn reddit
  "Reddit component, renders reddit feed and updates every minute"
  []
  (let [reddit-updater (js/setInterval
                        #(get-reddit! (:count @appdb))
                        60000)
        display-popover? (r/atom false)
        header-text (r/atom "Reddit")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Reddit"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))
        ]
    (r/create-class
     {:component-will-unmount #(js/clearInterval reddit-updater)
      :component-will-mount #(get-reddit! (:count @appdb))
      :reagent-render
      (fn []
        (let [open? @display-popover?]
          [:div
           {:class (:root reddit-style)}
           [:pre {:class (:header reddit-style)}
            @header-text]
           [:ul
            (for [node (:reddit @appdb)]
              ^{:key (gobj/getValueByKeys node "data" "id")}
              (let [title (truncate-string (gobj/getValueByKeys node "data" "title") 60)
                    id (gobj/getValueByKeys node "data" "id")
                    perma-link (gobj/getValueByKeys node "data" "permalink")]
                [:li {:on-mouse-enter #(reset! display-popover? true)
                      :on-mouse-leave #(reset! display-popover? false)
                      :key id}
                 [:a {:href (str "https://reddit.com" perma-link) :target "_blank"}
                  title]
                 [:div {:class (join-classes reddit-style :popover (when open? :popover-open))}]]))]]))})))

(defn get-org!
  "gets org nodes via an http request to server, and sets both org content in appdb
  and the count to use"
  []
  (go
    (let [resp (<! (http/get "/org"))]
      (swap! appdb assoc
             :org (:body resp)
             :count (count (:body resp))))))

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
   [:li {:cursor "pointer"}
    [:&:hover {:background (-> colors :bright-black :hex)
               :font-weight "bold"}]]]
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
  (let [org-updater (js/setInterval
                     get-org!
                     60000)
        header-text (r/atom "Todo list")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Todo list"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))]

    (r/create-class
     {:component-will-mount get-org!
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
           (:org @appdb))]])})))

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
           :height "100%"
           :justify-content "space-between"
           :margin-left (px 30)
           :margin-right (px 30)
           }])

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      [:div {:class (:root startpage-style)}
       (if (:count @appdb)
         [reddit])
       [clock]
       [org]])}))
