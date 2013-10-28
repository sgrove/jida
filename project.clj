(defproject jida "0.1.1"
  :plugins [[com.cemerick/austin "0.1.1"]
            [hiccup-bridge "1.0.0-SNAPSHOT"]
            [lein-ancient "0.5.1"]
            [lein-bikeshed "0.1.3"]
            [lein-cljsbuild "0.3.4"]
            [lein-kibit "0.0.8"]
            [lein-pprint "1.1.1"]
            [lein-ring "0.8.7"]
            [lein-typed "0.3.0"]
            [lein-vanity "0.1.0"]
            [org.timmc/nephila "0.2.0"]]
  :description "Hosted Community-Oriented Codeq"
  :dependencies [[cheshire "5.2.0"]
                 [cljs-ajax "0.2.1"]
                 [compojure "1.1.5"]
                 [com.cemerick/clojurescript.test "0.1.0"]
                 [com.datomic/datomic-free "0.8.4218"]
                 [hiccup "1.0.4"]
                 [korma "0.3.0-RC4"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1934"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.match "0.2.0"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [org.clojure/google-closure-library "0.0-20130212-95c19e7f0f5f"]
                 [org.clojure/google-closure-library-third-party "0.0-2029-2"]
                 [org.clojure/tools.reader "0.7.10"]
                 [org.clojure/tools.trace "0.7.6"]
                 [postgresql "9.1-901-1.jdbc4"]
                 [prismatic/dommy "0.1.2"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [sonian/carica "1.0.3"]]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"],
                :id "dev",
                :compiler
                {:pretty-print true,
                 :output-dir "resources/public/js/bin-debug",
                 :output-to "resources/public/js/bin-debug/main.js",
                 :optimizations :whitespace}}
               {:source-paths ["src/cljs"],
                :id "prod",
                :compiler
                {:output-dir "resources/public/js/bin",
                 :output-to "resources/public/js/bin/main.js",
                 :optimizations :simple}}]}
  :repl-options {:init-ns jida.client.main}
  :min-lein-version "2.0.0"
  :profiles {:prod {:resource-paths ["config/prod"]}
             :dev {:resource-paths ["config/dev"]
                   :source-paths ["test"]
                   :dependencies [[ring-mock "0.1.5"]]
                   :plugins []}
             :test{:resource-paths ["config/test"]
                   :source-paths ["test"]
                   :dependencies [[ring-mock "0.1.5"]]
                   :plugins []}
             :uberjar {:resource-paths ["config/prod"]
                       :aot :all}}
  :ring {:handler jida.server/app}
  :main jida.server)
