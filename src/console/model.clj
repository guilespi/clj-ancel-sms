(ns console.model
  (:require
   [clojure.java.jdbc :as sql]))

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "src/console/db/ancel.sqlite"})

(defn get-groups
  []
  (sql/with-connection db
    (sql/with-query-results rs
      ["SELECT * FROM groups"]
      (into [] rs))))

(defn add-group
  [group-name]
  (sql/with-connection db
    (sql/insert-records :groups {:name group-name})))

(defn rmv-group
  [group-name]
  (sql/with-connection db
    (sql/delete-rows :groups ["name=?" group-name])))

(defn get-group
  [group-name]
  (first (sql/with-connection db
           (sql/with-query-results results
             [(format "SELECT * FROM groups WHERE name='%s'" group-name)]
             (into [] results)))))

(defn get-phones
  []
  (sql/with-connection db
    (sql/with-query-results results
      ["SELECT * FROM phones"]
      (into [] results))))

(defn get-phone
  [number]
  (first (sql/with-connection db
           (sql/with-query-results results
             [(format "SELECT * FROM phones WHERE number='%s'" number)]
             (into [] results)))))

(defn add-phone
  [number]
  (sql/with-connection db
    (sql/insert-records :phones {:number number})))

(defn rmv-phone
  [number]
  (sql/with-connection db
    (sql/delete-rows :phones ["number=?" number])))

(defn add-phone-to-group
  [group-name number]
  (sql/with-connection db
    (sql/insert-records :group_phones {:group_name group-name :phone_number number})))

(defn remove-phone-from-group
  [group-name number]
  (sql/with-connection db
    (sql/delete-rows :group_phones ["group_name=? AND phone_number=?" group-name number])))

(defn get-group-phones
  [group-name]
  (sql/with-connection db
    (sql/with-query-results rs
      [(format "SELECT * FROM group_phones WHERE group_name='%s'" group-name)]
      (into [] rs))))