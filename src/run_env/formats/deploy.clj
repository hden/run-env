(ns run-env.formats.deploy
  (:require [clojure.string :as string]
            [run-env.formats.core :as core]))

(def ^:const synopsis "
gcloud run deploy [[SERVICE] --namespace=NAMESPACE] --image=IMAGE
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

(defn format-env [m]
  {:pre [(map? m)]}
  (when (< 0 (count m))
    (str "--update-env-vars="
         (string/join "," (into []
                                (map (fn [[k v]]
                                       (format "%s=%s" (name k) (core/interpolate (str v)))))
                                m)))))

(defn format-cloud-sql [coll]
  {:pre [(coll? coll)]}
  (when (< 0 (count coll))
    (str "--set-cloudsql-instances="
         (string/join "," (map core/interpolate coll)))))

(defn format-parameters [{:keys [name image] :as spec}]
  {:pre [(and (string? name)
              (string? image)
              (map? spec))]
   :post [(string? %)]}
  (string/join " " (into [(format "gcloud run deploy %s" (core/interpolate name))]
                         (comp (map (fn [x]
                                      (when x
                                        (if (string? x)
                                          x
                                          (core/optional x spec)))))
                               (filter some?))
                         (conj args
                               (core/optional :project spec)
                               (format-env (get spec :env {}))
                               (format-cloud-sql (get spec :cloudsql-instances []))))))
