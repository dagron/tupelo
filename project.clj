(defproject cljs-enflame "0.1.0-SNAPSHOT"
  :min-lein-version "2.7.1"
  :dependencies [
   ;[org.clojure/clojure "1.8.0"]
   ;[org.clojure/clojure "1.9.0"]
    [org.clojure/clojure "1.10.0-beta5"]
    [org.clojure/clojurescript "1.10.439"]

    [binaryage/devtools "0.9.4"]
    [binaryage/oops "0.6.2"]
    [cheshire "5.8.1"]
    [clj-time "0.15.1"]
    [cljs-ajax "0.7.5"]
    [clojure-csv/clojure-csv "2.0.2"]
    [criterium "0.4.4"]
    [danlentz/clj-uuid "0.1.7"]
    [enlive "1.1.6"]
    [joda-time/joda-time "2.10"] 
    [org.clojure/core.async "0.4.474"]
    [org.clojure/core.match "0.3.0-alpha4"]
    [org.clojure/data.xml "0.2.0-alpha5"]
    [org.clojure/math.combinatorics "0.1.4"]
    [org.clojure/spec.alpha "0.2.176"]
    [org.clojure/test.check "0.9.0"]
    [org.clojure/tools.analyzer "0.6.9"]
    [org.clojure/tools.reader "1.3.1"]
    [prismatic/schema "1.1.9"]
    [re-frame "0.10.6"]
    [reagent "0.8.1"]
    [reagent-utils "0.3.1"]
    [secretary "1.2.3"]
  ]
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]] 
            [lein-codox "0.10.3"]
            [lein-doo "0.1.10"]
            [lein-figwheel "0.5.17"]]

  :profiles {:dev {:dependencies [[com.jakemccrary/lein-test-refresh "0.23.0"]
                                 ;[binaryage/devtools "0.9.10"]
                                 ;[com.cemerick/piggieback "0.2.2"]
                                 ] }}
               ; :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :codox {:src-dir-uri               "http://github.com/cloojure/tupelo/blob/master/"
          :src-linenum-anchor-prefix "L"}

  :deploy-repositories {"snapshots"    :clojars
                        "releases"     :clojars
                        :sign-releases false}
  :doo {:karma {:config {"plugins"       ["karma-junit-reporter"]
                         "reporters"     ["progress" "junit"]
                         "junitReporter" {"outputDir" "target/test-results"}}}
        :paths {:karma   "node_modules/karma/bin/karma"
                :phantom "node_modules/phantomjs/bin/phantomjs"}}

  :global-vars {*warn-on-reflection*      false }

  :source-paths [ "src/cljc" "src/clj" ]
  :test-paths   [ "test/cljc" "test/clj" ]
  :target-path  "target/%s"

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths [ "src/cljc" "src/cljs" ]
                ; The presence of a :figwheel configuration here will cause figwheel to inject the
                ; figwheel client into your build
                :figwheel     {:on-jsload "flintstones.core/figwheel-reload"
                               ; :open-urls will pop open your application in the default browser once
                               ; Figwheel has started and compiled your application.  Comment this out
                               ; once it no longer serves you.
                               :open-urls ["http://localhost:3449/index.html"]}
                :compiler     {:main                 flintstones.core
                               :optimizations        :none
                               :libs                 ["resources/public/libs"] ; recursive includes all children

                               ; figwheel server has implicit path `resources/public`, leave off here
                               :foreign-libs         [{:file     "dino.js"
                                                       :provides ["dinoPhony"]}]
                               :externs              ["dino-externs.js"]

                               :output-to            "resources/public/js/compiled/flintstones.js"
                               :output-dir           "resources/public/js/compiled/flintstones-dev"
                               :asset-path           "js/compiled/flintstones-dev" ; rel to figwheel default of `resources/public`
                               ;                       ^^^^^ must match :output-dir

                               :source-map           true
                               :source-map-timestamp true}}
               {:id           "test"
                :source-paths [ "src/cljc" "test/cljc"
                                "src/cljs" "test/cljs" ] ; #todo  :test-paths ???
                :compiler     {:main                 tst.flintstones.doorunner
                               :optimizations        :none ; :advanced
                               :libs                 ["resources/public/libs"] ; recursively includes all children

                               ; tests run w/o figwheel server, so need to explicitely add path `resources/public` here
                               :foreign-libs         [{:file     "resources/public/dino.js"
                                                       :provides ["dinoPhony"]}]
                               :externs              ["resources/public/dino-externs.js"]

                               :output-to            "resources/public/js/compiled/bedrock.js"
                               :output-dir           "resources/public/js/compiled/bedrock-tst"
                               ; :asset-path           "js/compiled/bedrock-tst"  ; not used for testing
                               ; ^^^ rel to figwheel default of `resources/public`

                               :source-map           true
                               :source-map-timestamp true}}]}

  ; need to add the compliled assets to the :clean-targets
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "out"
                                    :target-path]

  ; "lein test"         will not  run tests marked with the ":slow" metadata
  ; "lein test :slow"   will only run tests marked with the ":slow" metadata
  ; "lein test :all"    will run all  tests (built-in)
  :test-selectors {:default (complement :slow)
                   :slow    :slow
                   :fast    :fast}
      ; #todo broken for tupelo.test/dospec - why?

  ; :main ^:skip-aot tupelo.core
  :uberjar      {:aot :all}

  ; automatically handle `--add-modules` stuff req'd for Java 9 & Java 10
  :jvm-opts ["-Xms500m" "-Xmx2g"
           ; "--illegal-access=permit"  ; may need for Java10+
            ] ; permit, warn, debug, deny

)
