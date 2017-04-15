(ns startpage.home
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [reagent.core :as r]
   [reagent.debug :as d]
   [cljsjs.react-jss]))

#_(defn init-figlet
  []
  (.defaults js/figlet #js {:fontPath "fonts"}))

#_(defn run-figlet
  [input ch]
  (js/figlet input
             "Fraktur"
             (fn [err text]
               (when err
                 (.log js/console "something went wrong")
                 (.dir js/console err))
               (when-not (nil? text)
                 (put! ch text)))))

(def styles {"@global" {:body {:margin 0
                               :font-family "'Lato Thin', sans-serif"
                               :background-color (-> colors :black :hex)}}
             :root {:color "white"
                    :display "flex"
                    :height "100vh"
                    :align-items "center"
                    :justify-content "center"}

             :clock {:font-size "50px"
                     ;; :border-bottom (str "0.02em solid " (-> colors :white :hex))
                     :color (-> colors :white :hex)}})

(defonce inject-sheet (.create js/reactJss))
(def style-wrapper (.default js/reactJss (clj->js styles)))

(defonce timer (r/atom (js/Date.)))
(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

#_(defn clock
  [classnames ascii]
  [:pre
   {:class (gobj/get classnames "clock")}
   ascii
   ])

(defn clock
  [classnames]
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:div
     {:class (gobj/get classnames "clock")}
     time-str
     ]))


(defn startpage
  []
  (let [
        ;; event-ch (chan)
        ;; time-str (-> @timer .toTimeString (clojure.string/split " ") first)
        ;; _ (add-watch timer :watcher (fn [_ _ _ new]
        ;;                               (let [time-str (-> new .toTimeString (clojure.string/split " ") first)]
        ;;                                 (run-figlet time-str event-ch))))
        ;; result (r/atom "")
        ;; event-loop (go
        ;;              (loop []
        ;;                (let [ascii (<! event-ch)]
        ;;                  (when-not (nil? ascii)
        ;;                    (reset! result ascii)
        ;;                    (recur)))))
        ]
    (r/create-class
     {
      ;; :component-will-mount #(init-figlet)
      :reagent-render
      (fn []
        (let [classnames (:classes (r/props (r/current-component)))]
          [:div {:class (gobj/get classnames "root")}
           [clock classnames]])
        )})))

(def app (r/adapt-react-class (style-wrapper (startpage))))

(defn ^:export main
  []
  (r/render [app] (. js/document (getElementById "app"))))
