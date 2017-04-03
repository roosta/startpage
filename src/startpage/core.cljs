(ns startpage.core
  (:require
   [cljsjs.react-jss]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [reagent.core :as r]
   [reagent.debug :as d]))

(def styles {"@global" {:body {:margin 0
                               ;; :font-family "'Ubuntu Mono', monospaced"
                               ;; :font-family "'VT323', monospaced"
                               :font-family "'Lato Thin', sans-serif"
                               :background-color (-> colors :black :hex)
                               }}
             :root {:color "white"
                    :display "flex"
                    :height "70vh"
                    :align-items "center"
                    :justify-content "center"}

             :clock {:font-size "10em"
                     :border-bottom (str "0.02em solid " (-> colors :white :hex))
                     :color (-> colors :white :hex)}})

(defonce inject-sheet (.create js/reactJss))
(def style-wrapper (inject-sheet (clj->js styles)))

(defonce timer (r/atom (js/Date.)))
(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

(defn clock
  [classnames]
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:div
     {:class (gobj/get classnames "clock")}
     time-str]))

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      (let [classnames (:classes (r/props (r/current-component)))]
        [:div {:class (gobj/get classnames "root")}
         [clock classnames]])
      )}))

(def app (r/adapt-react-class (style-wrapper (startpage))))

(defn ^:export main
  []
  (r/render [app] (. js/document (getElementById "app"))))
