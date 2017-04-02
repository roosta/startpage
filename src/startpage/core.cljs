(ns startpage.core
  (:require
   [cljsjs.react-jss]
   [goog.object :as gobj]
   [reagent.core :as r]
   [reagent.debug :as d]))

(def styles {:asd {:background-color "black"}})

(defonce inject-sheet (.create js/reactJss))
(def style-wrapper (inject-sheet (clj->js styles)))

(defn startpage
  []
  (let [classes (r/atom nil)]
    (r/create-class
     {:component-did-mount #(reset! classes (gobj/getValueByKeys % "props" "classes"))
      :reagent-render
      (fn []
        [:div {:class (gobj/get @classes "asd")}
         "asdadasd"]
        )})))

(def app (r/adapt-react-class (style-wrapper (startpage))))

(defn ^:export main
  []
  (r/render [app] (. js/document (getElementById "app"))))
