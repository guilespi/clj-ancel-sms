# clj-ancel-sms

A Clojure library wrapping the nefarious [Ancel SMS Empresa][1] API. In case you're unfortunate and have to deal with 
this thing, maybe this library can make your life a little bit better.

## Usage

The ANCEL API needs that you register all phones and groups you will be sending messages to.

### Administration

All administration APIs are in the `clj-ancel-sms.administration` module. Basically phones and groups management.

``` clojure
(ns test.admin
  (:require [clj-ancel-sms.administration :as admin])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(admin/create-group "1" "grupo1")
(admin/register-phone "1" "1" "099111222")
(admin/add-phone-to-group "1" "099111222" "grupo1")
(admin/delete-group "1" "grupo1")
...

```

### Messaging

SMSs can be sent to a single cellphone or to a group. In case the message is sent to a group
Ancel is doing the list expansion.

```clojure
(ns test.messages
  (:require [clj-ancel-sms.messaging :as messaging])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(messaging/to-cellphone "1" "099111222" "text message")
(messaging/to-group "1" "grupo1" "text message")

```

## Test Console

In `src/console` you'll find a testing console that also listens for callbacks on a jetty port.

    lein trampoline run -m console.cli

All the API commands are implemented for testing purposes

    > help
    Commands:
    help: Use help [command] for syntax info
    add-phone: Adds a new phone for sending messages
    show-group: Shows the current group phones
    send-message: Sends a SMS to a single phone
    rmv-from-group: Removes a number from a group
    list-groups: List the current defined groups
    rmv-phone: Removes a defined phone
    add-to-group: Adds a phone number to a group
    list-phones: List the current defined phones
    add-group: Adds a new empty group for sending messages
    send-to-group: Sends a SMS to a group
    rmv-group: Removes a defined group
    quit: Quits the console

## TODO

* Callback parsing inside the library

## License

Copyright © 2013 Guillermo Winkler

Distributed under the Eclipse Public License, the same as Clojure.


[1]: http://www.antel.com.uy/antel/empresas/movil/Servicios/con-costo/SMS-Empresas
