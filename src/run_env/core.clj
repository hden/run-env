(ns run-env.core
  (:require [clojure.data.json :as json]
            [run-env.formats.deploy :as deploy]
            [run-env.formats.delete :as delete])
  (:import [java.nio.file Path FileSystems])
  (:gen-class))

;; https://github.com/weavejester/medley/blob/1.1.0/src/medley/core.cljc#L174
(defn deep-merge
  ([])
  ([a] a)
  ([a b]
   (if (and (map? a) (map? b))
     (merge-with deep-merge a b)
     b))
  ([a b & more]
   (apply merge-with deep-merge a b more)))

(defn rel [^String x ^String y]
  {:pre [(and (string? x) (string? y))]
   :post [(string? %)]}
  (let [fs (FileSystems/getDefault)]
    (-> fs
        (.getPath x (into-array String []))
        (.resolveSibling y)
        (.normalize)
        str)))

(defn read-profile [s]
  {:pre [(string? s)]
   :post [(map? %)]}
  (let [m (json/read-str (slurp s) :key-fn keyword)
        targets (into []
                      (comp (map #(rel s %))
                            (map read-profile))
                      (get m :include []))]
    (apply deep-merge (conj targets m))))

(defn -main [cmd s]
  (let [formatter (condp = cmd
                    "deploy" deploy/format-parameters
                    "delete" delete/format-parameters)]
   (-> (read-profile s)
       formatter
       println)))
