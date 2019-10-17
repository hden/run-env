(ns run-env.core
  (:require [clojure.string :as string]
            [clojure.data.json :as json])
  (:import [java.nio.file Path FileSystems])
  (:gen-class))

(def ^:const synopsis "
gcloud beta run deploy [[SERVICE] --namespace=NAMESPACE] --image=IMAGE
    [--async] [--concurrency=CONCURRENCY] [--max-instances=MAX_INSTANCES]
    [--memory=MEMORY] [--platform=PLATFORM] [--timeout=TIMEOUT]
    [--clear-env-vars | --set-env-vars=[KEY=VALUE,...]
      | --remove-env-vars=[KEY,...] --update-env-vars=[KEY=VALUE,...]]
    [--clear-labels | --remove-labels=[KEY,...] --labels=[KEY=VALUE,...]
      | --update-labels=[KEY=VALUE,...]]
    [--cluster=CLUSTER --cluster-location=CLUSTER_LOCATION]
    [--connectivity=CONNECTIVITY --cpu=CPU]
    [--context=CONTEXT --kubeconfig=KUBECONFIG]
    [--[no-]allow-unauthenticated --region=REGION
      --service-account=SERVICE_ACCOUNT
      --add-cloudsql-instances=[CLOUDSQL-INSTANCES,...]
      | --clear-cloudsql-instances
      | --remove-cloudsql-instances=[CLOUDSQL-INSTANCES,...]
      | --set-cloudsql-instances=[CLOUDSQL-INSTANCES,...]]
    [GCLOUD_WIDE_FLAG ...]")

(def ^:const args (into []
                        (comp (map last)
                              (remove #(string/starts-with? % "clear-"))
                              (remove #(string/starts-with? % "set-"))
                              (remove #(string/starts-with? % "remove-"))
                              (remove #(string/starts-with? % "update-"))
                              (remove #(string/starts-with? % "add-"))
                              (map keyword))
                        (re-seq #"--(\[no-\])?([0-9a-z-]+)" synopsis)))

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

(defn format-env [m]
  {:pre [(map? m)]}
  (when (< 0 (count m))
    (str "--update-env-vars="
         (string/join "," (into []
                                (map (fn [[k v]]
                                       (format "%s=%s" (name k) (interpolate (str v)))))
                                m)))))

(defn format-cloud-sql [coll]
  {:pre [(coll? coll)]}
  (when (< 0 (count coll))
    (str "--set-cloudsql-instances="
         (string/join "," (map interpolate coll)))))

(defn format-parameters [{:keys [name image] :as spec}]
  {:pre [(and (string? name)
              (string? image)
              (map? spec))]
   :post [(string? %)]}
  (string/join " " (into [(format "gcloud beta run deploy %s" (interpolate name))]
                         (comp (map (fn [x]
                                      (when x
                                        (if (string? x)
                                          x
                                          (optional x spec)))))
                               (filter some?))
                         (conj args
                               (optional :project spec)
                               (format-env (get spec :env {}))
                               (format-cloud-sql (get spec :cloudsql-instances []))))))

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

(defn -main [s]
   (-> (read-profile s)
       format-parameters
       println))
