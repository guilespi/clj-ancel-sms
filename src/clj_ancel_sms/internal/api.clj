(ns clj-ancel-sms.internal.api
  (:refer-clojure :exclude [send])
  (:require [clj-http.client :as http]
            [clj-http.util :as util])
  (:use [clojure.string :only [join split]]))


(def ancel-util-endpoint "www.ancelutil.com.uy:8090")

(defn- encode-attributes
  [attributes]
  (join "&" (map #(str (util/url-encode (name (% 0))) "=" (util/url-encode (% 1))) attributes)))

(defn parse-error
  "Parses an API error assuming synax to be
   ERROR|CODE|DESCRIPTION
   If the response can't be parsed with that syntax
   the same input is returned"
  [response]
  (let [err (split response #"\|")]
    (if (= 3 (count err))
      {:error (err 0) :code (err 1) :description (err 2)}
      response)))

(defn execute
  [operation attributes]
  (let [url (format "http://%s/%s"
                    ancel-util-endpoint
                    (name operation))]
    (:body (http/post url
                      {:body (encode-attributes attributes)
                       :content-type "application/x-www-form-urlencoded"
                       :conn-timeout 5000
                       :socket-timeout 5000
                       :accept "text/plain"}))))