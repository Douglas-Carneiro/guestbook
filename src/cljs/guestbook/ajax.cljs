(ns guestbook.ajax
  (:require
   [ajax.core :refer [GET POST]]
   [re-frame.core :as rf]))

(rf/reg-fx
 :ajax/get
 (fn [{:keys [url success-event error-event success-path]}]
   (GET url
     (cond-> {:headers {"Accept" "application/transit+json"}}
       success-event (assoc :handler
                            #(rf/dispatch
                              (conj success-event
                                    (if success-path
                                      (get-in % success-path)
                                      %))))
       error-event
       (assoc :error-handler
              #(rf/dispatch
                (conj error-event %)))))))

(rf/reg-fx
 :ajax/post
 (fn [{:keys [url success-event error-event success-path params]}]
   (POST url
     (cond-> {:headers {"Accept" "application/transit+json"}}
       params
       (assoc :params params)
       success-event (assoc :handler
                            #(rf/dispatch
                              (conj success-event
                                    (if success-path
                                      (get-in % success-path)
                                      %))))
       error-event
       (assoc :error-handler
              #(rf/dispatch
                (conj error-event %)))))))
(rf/reg-fx
 :ajax/upload-media!
 (fn [{:keys [url success-event files handler]}]
   (.log js/console "Upload media effect dispatched")
   (.log js/console "Files: ")
   (.log js/console (clj->js files))
   (let [form-data (js/FormData.)]
     (doseq [[k v] files]
       (when (some? v)
         (.log js/console "File being processed ")
         (.log js/console (str "key: " k))
         (.log js/console (str "value: " v) (clj->js v))
         (.append form-data (name k) v)))
     (.log js/console "About to send the post request...")
     (.log js/console (clj->js url))
     (.log js/console (clj->js form-data))
     (.log js/console (.-keys form-data))
     (.log js/console (.-values form-data))
     (.log js/console (.getAll form-data "avatar"))
     (.log js/console (.getAll form-data "banner"))
     (.log js/console (str "Form data is null? " (empty? form-data)))
     (.log js/console (clj->js handler))
     (POST url {:body form-data
                :handler handler}))))
