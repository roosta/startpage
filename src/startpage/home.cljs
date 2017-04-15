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
                               :font-family "'Lato Thin', sans-serif"
                               :background-color (-> colors :black :hex)}}
             :root {:color "white"
                    :display "flex"
                    :height "100vh"
                    :align-items "center"
                    :justify-content "center"}

             :clock {:font-size "10px"
                     ;; :border-bottom (str "0.02em solid " (-> colors :white :hex))
                     :color (-> colors :white :hex)}})

(defonce inject-sheet (.create js/reactJss))
(def style-wrapper (.default js/reactJss (clj->js styles)))

(defonce timer (r/atom (js/Date.)))
(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

(defn clock
  [classnames ascii]
  [:pre
   {:class (gobj/get classnames "clock")}
   ascii
   ])

(defn watcher-fn
  [_ _ _ new ref]
  (let [time-str (-> new .toTimeString (clojure.string/split " ") first)]
    (go
      (when-let [resp (<! (http/post "/figlet" {:json-params {:text time-str}}))]
        (reset! ref (:body resp))
        )))
  )

(defn startpage
  []
  (let [ascii (r/atom "")]
    (r/create-class
     {:component-did-mount (fn []
                             (add-watch timer :watcher #(watcher-fn %1 %2 %3 %4 ascii)))
      :reagent-render
      (fn []
        (let [classnames (:classes (r/props (r/current-component)))]
          [:div {:class (gobj/get classnames "root")}
           [clock classnames @ascii]])
        )})))

(def app (r/adapt-react-class (style-wrapper (startpage))))

(defn ^:export main
  []
  (r/render [app] (. js/document (getElementById "app"))))
