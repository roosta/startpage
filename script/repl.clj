(use 'figwheel-sidecar.repl-api)
(start-figwheel! "server" "client") ;; <-- fetches configuration
(cljs-repl)
