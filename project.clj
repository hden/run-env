(defproject run-env (or (System/getenv "ARTIFACT_VERSION")
                        "0.1.0-SNAPSHOT")
  :description "Configure Cloud Run Deployment with a JSON file."
  :url "https://github.com/hden/run-env"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "0.2.7"]]
  :main ^:skip-aot run-env.core
  :target-path "target/%s"
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
  :profiles {:uberjar {:aot :all
                       :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]
                                      :opts ["--report-unsupported-elements-at-runtime"
                                             "--initialize-at-build-time"]}}})
