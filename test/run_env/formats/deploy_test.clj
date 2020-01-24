(ns run-env.formats.deploy-test
  (:require [clojure.test :refer :all]
            [run-env.formats.deploy :as deploy]))

(deftest format-env
  (testing "format-env"
    (are [expected m] (= expected (deploy/format-env m))
      nil {}
      "--update-env-vars=FOO=foo" {:FOO "foo"}
      "--update-env-vars=FOO=foo,BAR=bar" {:FOO "foo" :BAR "bar"})))

(deftest format-cloud-sql
  (testing "format-cloud-sql"
    (are [expected m] (= expected (deploy/format-cloud-sql m))
      nil []
      "--set-cloudsql-instances=foo" ["foo"]
      "--set-cloudsql-instances=foo,bar" ["foo" "bar"])))

(deftest format-parameters
  (testing "configurations"
    (are [expected m] (= expected (deploy/format-parameters m))
      "gcloud run deploy service --image=gcr.io/cloudrun/hello"
      {:name "service"
       :image "gcr.io/cloudrun/hello"}

      "gcloud run deploy service --image=gcr.io/cloudrun/hello --concurrency=10 --max-instances=1 --memory=512Mi --region=asia-northeast1 --update-env-vars=FOO=foo,BAR=bar"
      {:name "service"
       :image "gcr.io/cloudrun/hello"
       :concurrency 10
       :max-instances 1
       :memory "512Mi"
       :region "asia-northeast1"
       :env {:FOO "foo"
             :BAR "bar"}})))
