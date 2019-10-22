(ns run-env.formats.delete-test
  (:require [clojure.test :refer :all]
            [run-env.formats.delete :as delete]))

(deftest format-parameters
  (testing "configurations"
    (are [expected m] (= expected (delete/format-parameters m))
      "yes | gcloud beta run services delete service"
      {:name "service"
       :image "gcr.io/cloudrun/hello"}

      "yes | gcloud beta run services delete service --region=asia-northeast1"
      {:name "service"
       :image "gcr.io/cloudrun/hello"
       :concurrency 10
       :max-instances 1
       :memory "512Mi"
       :region "asia-northeast1"
       :env {:FOO "foo"
             :BAR "bar"}})))
