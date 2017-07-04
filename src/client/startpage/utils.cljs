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

(defn k-style-number
  "Takes a number and turns thousands into single digit with 'k' prepended
  e.g: 1000 = 1k"
  [number]
  (if (> number 999)
    (if (= (mod (/ number 100) 10) 0)
      (str (.floor js/Math (/ number 1000)) "k")
      (str (.floor js/Math (/ (/ number 100) 10.0)) "k"))
    number))
