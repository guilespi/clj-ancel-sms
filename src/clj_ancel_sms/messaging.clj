(ns clj-ancel-sms.messaging
  (:require [clj-ancel-sms.internal.api :as api])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(defn to-cellphone
  [service phone message]
  (if-let [number (re-matches #"09(\d{7})" phone)]
   (let [response (api/execute :envioSMS {:txtCelularNumero (number 1)
                                          :txtMensaje message
                                          :txtNroServicio service})]
     ;;I cannot believe this shit, how makes this an API OK response?
     (or (= response "El mensaje fue puesto en la cola para ser enviado\n")
         (throw+ {:type ::api-failure :response (api/parse-error response)})))
   (throw+ {:type ::invalid-number :number phone})))

(defn to-group
  [service group message]
  (let [response (api/execute :envioGrupo {:grupo group
                                           :mensaje message
                                           :servicio service})]
    ;;Yes, this OK response is different, go figure
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))