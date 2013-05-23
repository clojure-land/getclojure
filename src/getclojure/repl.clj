(ns getclojure.repl
  (:require [getclojure.handler :refer [app destroy init]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.server.standalone :refer [serve]]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ;; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ;; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)
      (wrap-params)))

(defn start-server
  "Used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 2600)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :init init
                    :auto-reload? true
                    :open-browser? false
                    :destroy destroy
                    :join true}))
    (println "Server started on port [" port "].")
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))