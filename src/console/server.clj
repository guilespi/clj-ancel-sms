(ns console.server
  (:require [ring.adapter.jetty :as jetty]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response])
  (:use ring.middleware.params
        ring.middleware.keyword-params
        ring.middleware.nested-params
        [compojure.core :only (GET PUT POST ANY defroutes context)]))

(defroutes main-routes
  (GET "/sms/events" [] (str "You have this working buddy!"))
  (POST "/sms/events" {params :params} (do
                                         (println (format "Received message %s" params))
                                         "OK"))
  (route/not-found (do (println "Unknown url received")
                       "Page not found")))

(def app
  (-> main-routes
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defn start
  []
    (println "Starting web server at http://localhost:8099/")
    (jetty/run-jetty app {:join? false :port 8099}))
