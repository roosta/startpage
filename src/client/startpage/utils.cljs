(ns startpage.utils)

(defn truncate-string
  [field max-length]
  (if (> (count field) max-length)
    (str (subs field 0 (dec max-length)) "...")
    field))

(defn join-classes
  [styles & classes]
  (->> (select-keys styles classes)
       vals
       (clojure.string/join " ")))

(defn transition
  [{:keys [prop duration timing-fn delay]
    :or {prop "all"
         duration "0s"
         timing-fn "ease"
         delay "0s"}}]
  {:transtion (clojure.string/join " " [prop duration timing-fn delay])})
