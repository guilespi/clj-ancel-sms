(ns clj-ancel-sms.administration-test
  (:require [clj-ancel-sms.administration :as admin]
            [clj-ancel-sms.internal.api :as api]
            [clj-http.client :as http])
  (:use midje.sweet
        [slingshot.slingshot :only [try+ throw+]]))

(fact "Group gets created OK"
      (admin/create-group "1" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=altaGrupo&servicio=1&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))

(fact "Group fails with an exeption if api fails"
      (try+
       (admin/create-group "1" "grupo1")
       (catch :type e
         e)) => {:type :clj-ancel-sms.administration/api-failure
                 :response {:error "ERROR" :code "102" :description "Failed creating"}}
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=altaGrupo&servicio=1&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "ERROR|102|Failed creating"} :times 1))


(fact "Group gets removed OK"
      (admin/delete-group "1" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=bajaGrupo&servicio=1&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Group exists query returning OK"
      (admin/group-exists? "1" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=consultaGrupo&servicio=1&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Register new phone"
      (admin/register-phone "1" "1" "099111222") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=altaServicio&servicio=1&celular=099111222&nroTramite=1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK|1234567890\n"} :times 1))


(fact "Unregister phone"
      (admin/unregister-phone "1" "1" "099111222") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=bajaServicio&servicio=1&celular=099111222&nroTramite=1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Check for registered phone"
      (admin/phone-registered? "1" "099111222") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=consultaServicioHabilitado&servicio=1&celular=099111222"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Add phone to group"
      (admin/add-phone-to-group "1" "099111222" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=altaCelularGrupo&servicio=1&celular=099111222&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Remove phone from group"
      (admin/rmv-phone-from-group "1" "099111222" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=bajaCelularGrupo&servicio=1&celular=099111222&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))


(fact "Check if phone in group"
      (admin/phone-in-group? "1" "099111222" "grupo1") => true
      (provided
       (http/post "http://www.ancelutil.com.uy:8090/admEmpresa"
                  {:body "operacion=consultaCelularGrupo&servicio=1&celular=099111222&grupo=grupo1"
                   :content-type "application/x-www-form-urlencoded"
                   :conn-timeout 5000
                   :socket-timeout 5000
                   :accept "text/plain"}) => {:body "OK\n"} :times 1))