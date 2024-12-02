(ns kit.spooky-town.domain.role-request.value
  (:require [clojure.spec.alpha :as s]))

;; Status
(s/def ::status #{:pending :approved :rejected})

(defn create-status 
  ([] :pending)  ;; 기본값
  ([status] 
   (when (s/valid? ::status status)
     status)))

;; Reason (요청/거절 사유: 10-500자)
(s/def ::reason (s/and string? 
                      #(>= (count %) 10)
                      #(<= (count %) 500)))

(defn create-reason [reason]
  (when (s/valid? ::reason reason)
    reason))

;; Timestamps
(s/def ::created-at inst?)
(s/def ::updated-at (s/nilable inst?))

(defn create-timestamp []
  (java.util.Date.))

(defn update-timestamp [entity]
  (assoc entity :updated-at (create-timestamp))) 