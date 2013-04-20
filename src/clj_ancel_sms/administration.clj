(ns clj-ancel-sms.administration
  (:require [clj-ancel-sms.internal.api :as api])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(defn create-group
  "Adds a new group"
  [service group-name]
  (let [response (api/execute :admEmpresa {:operacion "altaGrupo"
                                           :servicio service
                                           :grupo group-name})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn delete-group
  "Removes an existent group"
  [service group-name]
  (let [response (api/execute :admEmpresa {:operacion "bajaGrupo"
                                           :servicio service
                                           :grupo group-name})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn group-exists?
  "Checks if a group name is registered"
  [service group-name]
  (let [response (api/execute :admEmpresa {:operacion "consultaGrupo"
                                           :servicio service
                                           :grupo group-name})]
    (= response "OK\n")))

(defn register-phone
  "Registers a cellular phone in order for messages to be sent"
  [service tracking phone]
  (let [response (api/execute :admEmpresa {:operacion "altaServicio"
                                           :servicio service
                                           :celular phone
                                           :nroTramite tracking})
        ok-code (re-matches #"OK\|(\d+)\n" response)]
    (or (and ok-code true)
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn unregister-phone
  "Unregisters a cellular phone from the specified service"
  [service tracking phone]
  (let [response (api/execute :admEmpresa {:operacion "bajaServicio"
                                           :servicio service
                                           :celular phone
                                           :nroTramite tracking})]
    (= response "OK\n")))

(defn phone-registered?
  "Checks whether a phone is registered for a service or not"
  [service phone]
  (let [response (api/execute :admEmpresa {:operacion "consultaServicioHabilitado"
                                           :servicio service
                                           :celular phone})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn contract-status
  "Checks the contract status for a specified number, this operacion
   needs special permission to be executed"
  [phone]
  (let [response (api/execute :admEmpresa {:operacion "consultaEstadoContrato"
                                           :celular phone})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn add-phone-to-group
  "Adds a registered phone number to an existent group"
  [service phone group-name]
  (let [response (api/execute :admEmpresa {:operacion "altaCelularGrupo"
                                           :servicio service
                                           :celular phone
                                           :grupo group-name})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn rmv-phone-from-group
  "Removes a phone number from a group"
  [service phone group-name]
  (let [response (api/execute :admEmpresa {:operacion "bajaCelularGrupo"
                                           :servicio service
                                           :celular phone
                                           :grupo group-name})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))

(defn phone-in-group?
  "Checks if a phone number belongs to a group"
  [service phone group-name]
  (let [response (api/execute :admEmpresa {:operacion "consultaCelularGrupo"
                                           :servicio service
                                           :celular phone
                                           :grupo group-name})]
    (or (= response "OK\n")
        (throw+ {:type ::api-failure :response (api/parse-error response)}))))