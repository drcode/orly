(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
 {:figwheel-options {:server-port 80}
  :build-ids ["dev"]
  :all-builds
  [{:id "dev"
    :figwheel {:websocket-host "clojuredev.symptogram.com"}
    :source-paths ["src"]
    :compiler {:main 'orly-example.core
               :asset-path "js"
               :output-to "resources/public/js/main.js"
               :output-dir "resources/public/js"
               :verbose true}}]})

(ra/cljs-repl)
