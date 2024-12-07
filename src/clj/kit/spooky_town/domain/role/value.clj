(ns kit.spooky-town.domain.role.value
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

;; Role ID (ULID 형식)
(s/def ::role-id (s/and string? #(not (string/blank? %))))

;; Role Name - 시스템에서 사용되는 역할 식별자
(s/def ::role-name #{:user 
                     :admin 
                     :content-creator 
                     :sns-publisher 
                     :content-reviewer})

;; Description (선택적, 최대 500자)
(s/def ::description (s/nilable (s/and string?
                                      #(<= (count %) 500))))

;; Timestamps
(s/def ::created-at inst?)
(s/def ::updated-at inst?)

;; Value Object 생성 함수들
(defn create-role-name [name]
  (when (s/valid? ::role-name (keyword name))
    (keyword name)))

(defn create-description [description]
  (when (or (nil? description)
            (s/valid? ::description description))
    description))

(defn create-timestamp []
  (java.util.Date.))

;; 역할별 권한 체크 헬퍼 함수들
(def content-management-roles #{:admin :content-creator :content-reviewer})
(def sns-management-roles #{:admin :sns-publisher})
(def system-management-roles #{:admin})

(defn can-manage-content? [role-name]
  (contains? content-management-roles (keyword role-name)))

(defn can-publish-sns? [role-name]
  (contains? sns-management-roles (keyword role-name)))

(defn can-manage-system? [role-name]
  (contains? system-management-roles (keyword role-name))) 