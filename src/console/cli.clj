(ns console.cli
  (:gen-class)
  (:require [console.model :as model]
            [console.server :as server]
            [clj-ancel-sms.administration :as sms-admin]
            [clj-ancel-sms.messaging :as sms])
  (:use [clojure.tools.cli :only [cli]]
        [slingshot.slingshot :only [try+ throw+]]))

(def ^:dynamic *service* "2")
(def ^:dynamic *number* "99970")

(declare commands)

(defn- display-help
  ([]
     (println "Commands:")
     (doseq [c commands]
       (println (format "%s: %s"
                        (name (c 0))
                        ((c 1) 1))))
     (println))
  ([command]
     (if-let [command (get commands (keyword command))]
       (do
         (println (command 1))
         (println (str "Syntax => " (command 2))))
       (println "Unknown command: " command))))

(defn- add-group
  [group-name]
  (model/add-group group-name)
  (sms-admin/create-group *service* group-name)
  (println (str "Added group " group-name)))

(defn- list-groups
  []
  (let [groups (model/get-groups)]
    (println "Groups:")
    (doseq [g groups]
      (let [registered (try+
                        (sms-admin/group-exists? *service* (:name g))
                        (catch :type e
                          (println e)
                          false))]
        (println (format "%s - %s"
                         (:name g)
                         (if registered "Registered" "Unregistered")))))))

(defn- show-group
  [group-name]
  (println (format "Group %s members:" group-name))
  (let [members (model/get-group-phones group-name)]
    (doseq [phone members]
      (let [belongs (try+
                     (sms-admin/phone-in-group? *service* (:phone_number phone) group-name)
                     (catch :type e
                       (println e)
                       false))]
        (println (format "%s - %s"
                         (:phone_number phone)
                         (if belongs "In group" "Not in group")))))))

(defn- rmv-group
  [group-name]
  (model/rmv-group group-name)
  (sms-admin/delete-group *service* group-name)
  (println (format "Removed group %s" group-name)))

(defn- list-phones
  []
  (let [phones (model/get-phones)]
    (println "Phones:")
    (doseq [p phones]
      (let [registered (try+
                        (sms-admin/phone-registered? *service* (:number p))
                        (catch :type e
                          (println e)
                          false))]
        (println (format "%s - %s"
                         (:number p)
                         (if registered "Registered" "Unregistered")))))))

(defn- add-phone
  [number]
  (model/add-phone number)
  (sms-admin/register-phone *service* *service* number)
  (println (str "Added phone " number)))

(defn- rmv-phone
  [number]
  (model/rmv-phone number)
  (sms-admin/unregister-phone *service* *service* number)
  (println (str "Removed phone " number)))

(defn- add-to-group
  [group-name number]
  (if-let [phone (model/get-phone number)]
    (do
      (model/add-phone-to-group group-name number)
      (sms-admin/add-phone-to-group *service* number group-name) 
      (println (format "Added %s to group %s" number group-name)))
    (println (format "Phone number %s does not exist, create it first with add-phone..." number))))

(defn- rmv-from-group
  [group-name number]
  (model/remove-phone-from-group group-name number)
  (sms-admin/rmv-phone-from-group *service* number group-name) 
  (println (format "Removed %s from group %s" number group-name)))

(defn- send-message
  [number & args]
  (let [phone (model/get-phone number)
        message (clojure.string/join args)]
    (if phone
      (if (sms-admin/phone-registered? *service* number)
        (try+
         (sms/to-cellphone *service* number message)
         (println (format "Sent message '%s' to number %s" message number))
         (catch :type e
           (println "Error sending message to number:" e)))
        (println (format "Number %s is not registered, add it with add-phone" number)))
      (println (format "Number %s does not exist, have you added it with add-phone?" number)))))

(defn- send-to-group
  [group-name & args]
  (let [group (model/get-group group-name)
        message (clojure.string/join args)]
    (if group
      (if (sms-admin/group-exists? *service* group-name)
        (try+
         (sms/to-group *service* group-name message)
         (println (format "Sent message '%s' to group %s" message group-name))
         (catch :type e
           (println "Error sending message to group:" e)))
        (println (format "Group %s is not registered, add it with add-group" group-name)))
      (println (format "Group %s does not exist, have you added it with add-group?")))))


(defn- quit-console
  []
  (java.lang.System/exit 0))

(def commands {:help [display-help "Use help [command] for syntax info" "help [command]"]
               :list-groups [list-groups "List the current defined groups" "list-groups"]
               :add-group [add-group "Adds a new empty group for sending messages" "add-group [group-name]"]
               :show-group [show-group "Shows the current group phones" "show-group [group-name]"]
               :rmv-group [rmv-group "Removes a defined group" "rmv-group [group-name]"]
               :list-phones [list-phones "List the current defined phones" "list-phones"]
               :add-phone [add-phone "Adds a new phone for sending messages" "add-phone [number]"]
               :rmv-phone [rmv-phone "Removes a defined phone" "rmv-phone [number]"]
               :add-to-group [add-to-group "Adds a phone number to a group" "add-to-group [group-name] [number]"]
               :rmv-from-group [rmv-from-group "Removes a number from a group" "rmv-from-group [group-name] [number]"]
               :send-message [send-message "Sends a SMS to a single phone" "send-message [phone] [message]"]
               :send-to-group [send-to-group "Sends a SMS to a group" "send-to-group [group] [message]"]
               :quit [quit-console "Quits the console" "quit"]})

(defn valid-command?
  [command]
  (let [tokens (clojure.string/split command #"\s+")
        command (get commands (keyword (tokens 0)))]
    (when command
      [(command 0) (rest tokens)])))

(defn get-input
  [prompt]
  (print prompt)
  (flush)
  (clojure.string/trim (read-line)))

(defn- repl
  []
  (println "Enter 'help' for available commands")
  (loop [input (get-input "> ")]
    (let [command (valid-command? input)]
      (when (> (count input) 0)
        (if command
          (try+
           (apply (command 0) (command 1))
           (catch Object _
             (println (str "Error processing command: " (str _)))))
          (do
            (println "Unknown command: " input))))
      (recur (get-input "> ")))))

(defn start
  []
  (server/start)
  (repl))

(defn -main
  "Run me"
  [& args]
  (let [[options args banner] (cli args
                                   ["-s" "--service" "service to use" :default "2"]
                                   ["-n" "--number" "number to use" :default "99970"])]
    (binding [*service* (:service options)
              *number* (:number options)]
      (server/start)
      (repl))))