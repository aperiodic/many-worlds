(ns many-worlds.api)

(defn parse-frame-opts
  ([opts] (parse-frame-opts opts nil))
  ([opts state]
   (let [{t-str :t, s-str :s} opts
         s (let [s (read-string (or s-str "latest"))]
             (if (and (number? s) (not (zero? s)))
               (/ s 100)
               1))
         t (let [t (read-string (or t-str "latest"))]
             (if (number? t)
               t
               (or (-> state :path keys last) 0)))]
     {:s s, :t t})))
