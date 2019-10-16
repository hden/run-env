(ns run-env.core-test
  (:require [clojure.test :refer :all]
            [run-env.core :as core]))

(deftest helpers
  (testing "optional"
    (are [expected k] (= expected (core/optional k {:foo "bar"}))
      nil :bar
      "--foo=bar" :foo)
    (is (= "--bool") (core/optional :bool {:bool true})))

  (testing "format-env"
    (are [expected m] (= expected (core/format-env m))
      nil {}
      "--update-env-vars=FOO=foo" {:FOO "foo"}
      "--update-env-vars=FOO=foo,BAR=bar" {:FOO "foo" :BAR "bar"})))

(deftest format-parameters
  (testing "configurations"
    (are [expected m] (= expected (core/format-parameters m))
      "gcloud beta run deploy service --image=gcr.io/cloudrun/hello"
      {:name "service"
       :image "gcr.io/cloudrun/hello"}

      "gcloud beta run deploy service --image=gcr.io/cloudrun/hello --concurrency=10 --max-instances=1 --memory=512Mi --region=asia-northeast1 --update-env-vars=FOO=foo,BAR=bar"
      {:name "service"
       :image "gcr.io/cloudrun/hello"
       :concurrency 10
       :max-instances 1
       :memory "512Mi"
       :region "asia-northeast1"
       :env {:FOO "foo"
             :BAR "bar"}})))
