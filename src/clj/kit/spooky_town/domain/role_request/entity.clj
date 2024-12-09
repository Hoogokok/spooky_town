(ns kit.spooky-town.domain.role-request.entity
  (:require [kit.spooky-town.domain.role-request.value :as value]
            [kit.spooky-town.domain.role.value :as role-value]
            [clojure.spec.alpha :as s]))

;; Specs
(s/def ::id pos-int?)                    ;; 내부 ID (DB)
(s/def ::uuid uuid?)                     ;; 외부 노출용 ID
(s/def ::user-id string?)               ;; User ULID
(s/def ::current-role ::role-value/role-name)
(s/def ::requested-role ::role-value/role-name)
(s/def ::reason ::value/reason)
(s/def ::status ::value/status)
(s/def ::created-at ::value/created-at)
(s/def ::updated-at ::value/updated-at)
(s/def ::approved-by (s/nilable string?))  ;; Admin ULID
(s/def ::rejected-by (s/nilable string?))  ;; Admin ULID
(s/def ::rejection-reason (s/nilable ::value/reason))

(s/def ::role-request
  (s/keys :req-un [::uuid ::user-id ::current-role ::requested-role ::reason ::status ::created-at]
          :opt-un [::id ::updated-at ::approved-by ::rejected-by ::rejection-reason]))
;; Entity functions
(defn create-role-request
  [{:keys [user-id current-role requested-role reason]}]
  (when (and user-id current-role requested-role (value/create-reason reason))
    {:uuid (random-uuid)
     :user-id user-id
     :current-role current-role
     :requested-role requested-role
     :reason reason
     :status (value/create-status)
     :created-at (value/create-timestamp)}))

(defn approve-request
  [{:keys [status] :as request} admin-id]
  (when (and request (= status :pending) admin-id)
    (-> request
        (assoc :status :approved
               :approved-by admin-id)
        value/update-timestamp)))

(defn reject-request
  [{:keys [status] :as request} admin-id rejection-reason]
  (when (and request 
             (= status :pending) 
             admin-id 
             (value/create-reason rejection-reason))
    (-> request
        (assoc :status :rejected
               :rejected-by admin-id
               :rejection-reason rejection-reason)
        value/update-timestamp)))

(defn pending? [request]
  (= (:status request) :pending))

(defn approved? [request]
  (= (:status request) :approved))

(defn rejected? [request]
  (= (:status request) :rejected)) 