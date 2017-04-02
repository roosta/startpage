(ns startpage.core
  (:require
   [cljsjs.react-jss]
   [goog.object :as gobj]
   [reagent.core :as r]
   [reagent.debug :as d]))

(def styles {"@global" {:body {:background-color "blue"}}
             :asd {:color "white"
                   :background-color "black"}})

(defonce inject-sheet (.create js/reactJss))
(def style-wrapper (inject-sheet (clj->js styles)))

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      (let [classnames (:classes (r/props (r/current-component)))]
        [:div {:class (gobj/get classnames "asd")}
         "asdadasd"])
      )}))

(def app (r/adapt-react-class (style-wrapper (startpage))))

(defn ^:export main
  []
  (r/render [app] (. js/document (getElementById "app"))))
