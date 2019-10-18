(ns run-env.formats.core
  (:require [clojure.string :as string]))

(defn interpolate [s]
  {:pre [(string? s)]
   :post [(string? %)]}
  (string/replace s #"\$([1-9A-Z_]+)" (fn [[_ x]]
                                        (or (System/getenv x)
                                            ""))))

(defn optional [k m]
  {:pre [(and (keyword? k)
              (map? m))]}
  (let [x (get m k)]
    (when x
      (if (true? x)
        (format "--%s" (name k))
        (format "--%s=%s" (name k) (interpolate (str x)))))))
