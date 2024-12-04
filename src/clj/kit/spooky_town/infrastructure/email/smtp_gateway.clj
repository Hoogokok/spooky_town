(ns kit.spooky-town.infrastructure.email.smtp-gateway
  (:require [kit.spooky-town.domain.user.gateway.email :as email-gateway]
            [postal.core :as postal]
            [integrant.core :as ig]))

(def templates
  {:registration
   {:subject "스푸키 타운 회원가입 인증"
    :body (fn [url] 
            {:text (str "회원가입을 완료하려면 다음 링크를 클릭하세요: " url)
             :html (str "<p>회원가입을 완료하려면 다음 링크를 클릭하세요:</p>"
                       "<p><a href='" url "'>회원가입 인증하기</a></p>")})}
   
   :password-reset
   {:subject "스푸키 타운 비밀번호 초기화"
    :body (fn [url]
            {:text (str "비밀번호를 초기화하려면 다음 링크를 클릭하세요: " url)
             :html (str "<p>비밀번호를 초기화하려면 다음 링크를 클릭하세요:</p>"
                       "<p><a href='" url "'>비밀번호 초기화하기</a></p>")})}
   
   :email-change
   {:subject "스푸키 타운 이메일 변경 인증"
    :body (fn [url]
            {:text (str "이메일 변경을 완료하려면 다음 링크를 클릭하세요: " url)
             :html (str "<p>이메일 변경을 완료하려면 다음 링크를 클릭하세요:</p>"
                       "<p><a href='" url "'>이메일 변경 인증하기</a></p>")})}})

(defrecord SMTPEmailGateway [smtp-config base-url]
  email-gateway/EmailGateway
  (send-verification-email [_ email token]
    (let [url (str base-url "/verify-email?token=" token)
          template (get-in templates [:registration])
          body ((:body template) url)]
      (postal/send-message smtp-config
                          {:from (:from smtp-config)
                           :to email
                           :subject (:subject template)
                           :body [:alternative body]})))
  
  (send-password-reset-email [_ email token]
    (let [url (str base-url "/reset-password?token=" token)
          template (get-in templates [:password-reset])
          body ((:body template) url)]
      (postal/send-message smtp-config
                          {:from (:from smtp-config)
                           :to email
                           :subject (:subject template)
                           :body [:alternative body]})))
  
  (send-email-change-verification [_ email token]
    (let [url (str base-url "/verify-email-change?token=" token)
          template (get-in templates [:email-change])
          body ((:body template) url)]
      (postal/send-message smtp-config
                          {:from (:from smtp-config)
                           :to email
                           :subject (:subject template)
                           :body [:alternative body]}))))

(defmethod ig/init-key :infrastructure/email-gateway
  [_ {:keys [smtp-config base-url]}]
  (->SMTPEmailGateway smtp-config base-url)) 