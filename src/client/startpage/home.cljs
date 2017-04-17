(ns startpage.home
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [cljs-http.client :as http]
   [reagent.core :as r]
   [reagent.debug :as d]
   [cljsjs.react-jss]))

(def styles {"@global" {:body {:margin 0
                               :font-family "monospace"
                               :background-color (-> colors :black :hex)}}
             :root {:color "white"
                    :display "flex"
                    :justify-content "space-around"
                    :height "100vh"}

             :reddit-feed {}
             :org-root {:color (-> colors :bright-white :hex)}

             :clock-root {:font-size "10px"
                          ;; :position "relative"
                          :width "450px"
                          ;; :border-bottom (str "0.02em solid " (-> colors :bright-white :hex))
                          :color (-> colors :bright-white :hex)}
             :clock-child {
                           ;; :position "absolute"
                           }})

(defonce org (r/atom []))
(defn get-org!
  []
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! org (:body resp)))))

(def style-wrapper (.default js/reactJss (clj->js styles)))

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

(defn org-component
  [classnames data]
  (d/log data)
  [:div
   {:class (gobj/get classnames "org-root")}
   [:ul]
   (for [node data]
     ^{:key (:headline node)}
     [:li (:headline node)]
     )
   ])

(defn clock
  [classnames ascii]
  [:div
   {:class (gobj/get classnames "clock-root")}
   [:pre
    {:class (gobj/get classnames "clock-child")}
    ascii]])

(defn watcher-fn
  [_ _ _ new ref]
  (let [time-str (-> new .toTimeString (clojure.string/split " ") first)]
    (go
      (when-let [resp (<! (http/post "/figlet" {:json-params {:text time-str}}))]
        (reset! ref (:body resp))))))

(defn startpage*
  []
  (let [ascii (r/atom "")]
    (r/create-class
     {:component-did-mount (fn []
                             (get-org!)
                             (add-watch timer :watcher #(watcher-fn %1 %2 %3 %4 ascii)))
      :reagent-render
      (fn []
        (let [classnames (:classes (r/props (r/current-component)))]
          [:div {:class (gobj/get classnames "root")}
           [org-component classnames @org]
           [clock classnames @ascii]
           [reddit-feed]
           ])
        )})))

(def startpage (r/adapt-react-class (style-wrapper (startpage*))))
