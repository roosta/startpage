(ns startpage.home
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [startpage.utils :refer [k-style-number truncate-string join-classes transition]]
   [cljs-http.client :as http]
   [goog.string :as gstr]
   [garden.units :as u :refer [px pt pc]]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d]
   [figwheel.client.utils :as utils]
   [goog.array :as garr])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce appdb (r/atom {:org nil
                        :show-details? false
                        :reddit-node nil
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
         :margin 0
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
        header-text (r/atom "Reddit")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Reddit"
                                                               :font "Standard"}}))]
              (reset! header-text (:body resp))))
        timer (r/atom nil)]
    (r/create-class
     {:component-will-unmount #(js/clearInterval reddit-updater)
      :component-will-mount #(get-reddit! (:count @appdb))
      :reagent-render
      (fn []
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
              [:li {:on-mouse-enter (fn []
                                      (let [show-details? (and (.-data node) (.. node -data -preview))]
                                        (reset! timer (js/setTimeout (fn []
                                                                       (swap! appdb assoc
                                                                              :show-details? true
                                                                              :reddit-node node
                                                                              ))
                                                                     500))))
                    :on-mouse-leave (fn []
                                      (js/clearTimeout @timer)
                                      (swap! appdb assoc :show-details? false))
                    :key id}
               [:a {:href (str "https://reddit.com" perma-link) :target "_blank"}
                title]]))]])})))

(defn get-org!
  "gets org nodes via an http request to server, and sets both org content in appdb
  and the count to use"
  []
  (go
    (let [resp (<! (http/get "/org"))]
      (swap! appdb assoc
             :org (:body resp)
             :count (count  (filter #(= (:level %) 1) (:body resp)))))))

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
        header-text (r/atom "Todos")
        _ (go
            (let [resp (<! (http/post "/figlet" {:json-params {:text "Todos"
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

(defstyle details-style
  [:.root {:width 460
           :height 540
           :justify-content "center"
           :position "relative"
           :display "flex"}]
  [:.preview-img {:width 455
                  :position "absolute"
                  :border-radius "50%"
                  :top 2
                  :object-fit "cover"
                  :z-index -1
                  :height 462}]
  [:.info {:position "absolute"
           :bottom 15}]
  [:.heading {:font-size (px 20)
            :margin-right (px 14)}]
  [:.icon {:object-fit "none"
           :margin-top "-43px"}]
  [:.subheading {:font-size (px 14)
                 :color (-> colors :white :hex)}]
  [:.circle {:position "absolute"
             :width "100%"
             :border "none"
             :top 0}])


(defn details
  []
  (let [node (:reddit-node @appdb)
        score (k-style-number (gobj/getValueByKeys node "data" "score"))
        subreddit-name (gobj/getValueByKeys node "data" "subreddit_name_prefixed")
        num-comments (gobj/getValueByKeys node "data" "num_comments")
        img-obj (first (gobj/getValueByKeys node "data" "preview" "images"))]
    [:div {:class (:root details-style)}
     [:img {:class (:circle details-style)
            :src "/img/circle.png"}]
     (if img-obj
       (if (garr/isEmpty (gobj/get img-obj "resolutions"))
         (let [img (gobj/getValueByKeys img-obj "source")
               url (gstr/unescapeEntities (gobj/get img "url"))]
           [:img {:src url
                  :class (:preview-img details-style)}])
         (let [
               ;;TODO choose relative
               img (last (gobj/get img-obj "resolutions"))

               url (gstr/unescapeEntities (gobj/get img "url"))]
           [:img {:src url
                  :class (:preview-img details-style)}]))
       (case (gobj/getValueByKeys node "data" "thumbnail")
         "self" [:img {:class (:icon details-style)
                       :src "/img/self_icon.png"}]
         [:img {:class (:icon details-style)
                :src "/img/default_icon.png"}]))
     [:span {:class (:info details-style)}
      [:span {:class (:heading details-style)}
       score [:span {:class (:subheading details-style)} "pts"]]
      [:span {:class (:heading details-style)}
       num-comments
       [:span {:class (:subheading details-style)} "comments"]]
      [:span {:class (:heading details-style)}
       subreddit-name]]]))

(defstyle clock-style
  [:.root {:font-size (px 10)
           :display "flex"
           :flex-direction "column"
           :flex-basis "33.3333333%"
           :max-width "33.3333333%"
           :align-items "center"}
   [:pre {:margin 0
          :margin-top (px 20)}]]
  [:.paxel {:width 460
            :height 540}])

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

         #_[details]
         (if (:show-details? @appdb)
           [details]
           [:img {:class (:paxel clock-style)
                  :src "/img/paxel.png"}])
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
       (when (:count @appdb)
         [reddit])
       [clock]
       [org]])}))
