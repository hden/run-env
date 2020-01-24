(ns run-env.formats.delete
  (:require [clojure.string :as string]
            [run-env.formats.core :as core]))

(def ^:const synopsis "
gcloud run services delete (SERVICE : --namespace=NAMESPACE)
    [--platform=PLATFORM] [--region=REGION]
    [--cluster=CLUSTER --cluster-location=CLUSTER_LOCATION]
    [--context=CONTEXT --kubeconfig=KUBECONFIG] [GCLOUD_WIDE_FLAG ...]")

(def ^:const args (into []
                        (comp (map last)
                              (map keyword))
                        (re-seq #"--(\[no-\])?([0-9a-z-]+)" synopsis)))

(defn format-parameters [{:keys [name image] :as spec}]
  {:pre [(and (string? name)
              (string? image)
              (map? spec))]
   :post [(string? %)]}
  (string/join " " (into [(format "yes | gcloud run services delete %s" (core/interpolate name))]
                         (comp (map (fn [x]
                                      (when x
                                        (if (string? x)
                                          x
                                          (core/optional x spec)))))
                               (filter some?))
                         (conj args
                               (core/optional :project spec)))))
