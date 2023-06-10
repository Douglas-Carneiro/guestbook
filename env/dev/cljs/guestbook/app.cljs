(ns ^:dev/once guestbook.app
    (:require
     [devtools.core :as devtools]
     [guestbook.core :as core]))

(enable-console-print!)

(devtools/install!)

(core/init!)