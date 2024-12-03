(ns kit.spooky-town.domain.user.entity
  (:require [kit.spooky-town.domain.user.value :as value]
            [clojure.spec.alpha :as s]))

;; Entity Spec
(s/def ::user
  (s/keys :req-un [::value/uuid
                   ::value/email
                   ::value/name
                   ::value/hashed-password
                   ::value/roles
                   ::value/created-at]
          :opt-un [::value/updated-at
                  ::value/deleted-at
                  ::value/withdrawal-reason]))

;; User 도메인 로직을 위한 프로토콜


(defrecord User [uuid
                 email
                 name
                 hashed-password
                 roles
                 created-at
                 updated-at
                 deleted-at
                 withdrawal-reason]
)

(defn create-user
  "새로운 User Entity를 생성합니다."
  [{:keys [uuid email name hashed-password created-at]}]
  (let [user (map->User
              {:uuid uuid
               :email email
               :name name
               :hashed-password hashed-password
               :roles (value/create-roles)
               :created-at created-at
               :updated-at nil
               :deleted-at nil
               :withdrawal-reason nil})]
    (when (s/valid? ::user user)
      user)))

;; Entity Operations
(defn update-email
  "사용자 이메일을 업데이트합니다."
  [^User user new-email]
  (when-let [validated-email (value/create-email new-email)]
    (map->User (assoc user :email validated-email))))

(defn update-name
  "사용자 이름을 업데이트합니다."
  [^User user new-name]
  (when-let [validated-name (value/create-name new-name)]
    (map->User (assoc user :name validated-name))))

(defn update-password
  "사용자 비밀번호를 업데이트합니다."
  [^User user new-hashed-password]
  (map->User (assoc user :hashed-password new-hashed-password)))

(defn add-role
  "사용자에게 새로운 역할을 추가합니다."
  [^User user role]
  (when-let [updated-roles (value/add-role (:roles user) role)]
    (map->User (assoc user :roles updated-roles))))

(defn remove-role
  "사용자의 역할을 제거합니다."
  [^User user role]
  (when-let [updated-roles (value/remove-role (:roles user) role)]
    (map->User (assoc user :roles updated-roles))))

(defn admin? [^User user]
  (value/has-role? (:roles user) :admin))

(defn moderator? [^User user]
  (value/has-role? (:roles user) :moderator))

(defn has-role? [^User user role]
  (value/has-role? (:roles user) role))

(defn mark-as-withdrawn
  "사용자를 탈퇴 처리합니다."
  [^User user reason]
  (map->User (merge user
                    {:deleted-at (value/create-timestamp)
                     :withdrawal-reason reason})))

(defn withdrawn?
  "사용자가 탈퇴했는지 확인합니다."
  [^User user]
  (some? (:deleted-at user)))