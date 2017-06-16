(defproject edn-diff "0.1.0-SNAPSHOT"
  :description "A edn diff library for Clojure."
  :url "https://github.com/simonlevesque/edn-diff"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.562"]]

  :plugins [[lein-kibit "0.1.5"]
            [lein-cljsbuild "1.1.6"]]

  :profiles {:dev {:dependencies [[spyscope "0.1.6"]]
                   :injections [(require 'spyscope.core)]}}

  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:output-to "src-js/build.js"
                                   :optimizations :advanced}}]})
