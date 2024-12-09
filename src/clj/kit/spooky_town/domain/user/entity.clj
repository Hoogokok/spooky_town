(ns kit.spooky-town.domain.user.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.user.value :as value]))

;; User 레코드 정의
(defrecord User [user-id 
                 uuid 
                 email 
                 name 
                 hashed-password 
                 created-at 
                 updated-at 
                 deleted-at 
                 withdrawal-reason])

;; User 스펙 정의
(s/def ::user
  (s/keys :req-un [::value/user-id
                   ::value/uuid
                   ::value/email
                   ::value/name
                   ::value/hashed-password
                   ::value/created-at]
          :opt-un [::value/updated-at
                  ::value/deleted-at
                  ::value/withdrawal-reason]))

;; User 생성
(defn create-user [{:keys [user-id uuid email name hashed-password
                          created-at updated-at deleted-at withdrawal-reason]
                   :or {created-at (value/create-timestamp)}}]
  (let [user (map->User {:user-id user-id
                        :uuid uuid
                        :email email
                        :name name
                        :hashed-password hashed-password
                        :created-at created-at
                        :updated-at updated-at
                        :deleted-at deleted-at
                        :withdrawal-reason withdrawal-reason})]
    (when (s/valid? ::user user)
      user)))

;; 업데이트 메서드들
(defn update-email [^User user new-email]
  (when-let [validated-email (value/create-email new-email)]
    (map->User (assoc user :email validated-email))))

(defn update-name [^User user new-name]
  (when-let [validated-name (value/create-name new-name)]
    (map->User (assoc user :name validated-name))))

(defn update-password [^User user new-hashed-password]
  (map->User (assoc user :hashed-password new-hashed-password)))

;; 탈퇴 관련 메서드들
(defn mark-as-withdrawn [^User user reason]
  (map->User (merge user
                    {:deleted-at (value/create-timestamp)
                     :withdrawal-reason reason})))

(defn withdrawn? [^User user]
  (some? (:deleted-at user)))
