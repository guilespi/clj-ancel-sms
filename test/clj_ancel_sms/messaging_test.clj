(ns clj-ancel-sms.messaging-test
  (:require [clj-ancel-sms.messaging :as messaging]
            [clj-ancel-sms.internal.api :as api]
            [clj-http.client :as http])
  (:use midje.sweet
        [slingshot.slingshot :only [try+ throw+]]))

(fact "Message to single phone is sent OK"
      (messaging/to-cellphone "1" "099111222" "text message") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/envioSMS"
                  {:body "txtCelularNumero=9111222&txtMensaje=text+message&txtNroServicio=1"
                   :content-type "application/x-www-form-urlencoded;charset=UTF-8"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "El mensaje fue puesto en la cola para ser enviado\n"} :times 1))

(fact "Send message shows an unparsable exception"
      (try+
       (messaging/to-cellphone "1" "099111222" "text message")
       (catch :type e
         e)) => {:type :clj-ancel-sms.messaging/api-failure
                 :response "Random unparsable failure\n"}
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/envioSMS"
                  {:body "txtCelularNumero=9111222&txtMensaje=text+message&txtNroServicio=1"
                   :content-type "application/x-www-form-urlencoded;charset=UTF-8"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "Random unparsable failure\n"} :times 1))


(fact "Message is delivered to group correctly"
      (messaging/to-group "1" "grupo1" "text message") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/envioGrupo"
                  {:body "grupo=grupo1&mensaje=text+message&servicio=1"
                   :content-type "application/x-www-form-urlencoded;charset=UTF-8"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))