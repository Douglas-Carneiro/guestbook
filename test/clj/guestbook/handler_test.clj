(ns guestbook.handler-test
  (:require [buddy.hashers :as hashers]
            [clojure.test :refer :all]
            [guestbook.config :refer :all]
            [guestbook.db.core :as db]
            [guestbook.handler :refer :all]
            [guestbook.middleware.formats :as formats]
            [guestbook.routes.websockets :refer :all]
            [mount.core :as mount]
            [muuntaja.core :as m]
            [ring.mock.request :refer :all]
            [clojure.instant :refer [read-instant-date]]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'guestbook.config/env
                 #'guestbook.routes.websockets/socket
                 #'guestbook.handler/routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(defn login-request [login pass]
  (-> (request :post "/api/login")
      (json-body {:login login
                  :password pass})))

(def now (java.util.Date. 0))

(defn mock-user [{:keys [login]}]
  (when (= login "foo")
    {:login "foo"
     :password (hashers/encrypt "password")
     :created_at now
     :profile {}}))

(deftest test-login
  (with-redefs [db/get-user-for-auth* mock-user]
    (let [handler (app)]
      (testing "login success"
        (let [{:keys [body status] :as r} (handler
                                           (login-request "foo" "password"))
              json (parse-json body)]
          (println (assoc r :body json))
          (is (= 200 status))
          (is (= (->
                  (:identity json)
                  (update :created_at read-instant-date))
                 (-> {:login "foo"}
                     mock-user
                     (dissoc :password))))))
      (testing "password mismatch"
        (let [{:keys [status]} (handler (login-request "foo" "hacker"))]
          (is (= 401 status)))))))