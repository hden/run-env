(ns run-env.formats.core-test
  (:require [clojure.test :refer :all]
            [run-env.formats.core :as core]))

(deftest core
  (testing "optional"
    (are [expected k] (= expected (core/optional k {:foo "bar"}))
      nil :bar
      "--foo=bar" :foo)
    (is (= "--bool") (core/optional :bool {:bool true}))))
