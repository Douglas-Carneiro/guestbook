(ns guestbook.validation
  (:require
   [struct.core :as st]
   [clojure.string :as string]))

(def message-schema
  [[:name
    st/required
    st/string
    {:message "name must contain at least 4 characters"
     :validate (fn [msg] (>= (count msg) 4))}]
   [:message
    st/required
    st/string
    {:message "message must contain at least 10 characters and can't be only whitespace"
     :validate (fn [msg] (and (>= (count msg) 10) (not (string/blank? msg))))}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))