(ns kit.spooky-town.web.middleware.auth
  (:require [kit.spooky-town.domain.user.gateway.token :as token]
            [failjure.core :as f]
            [clojure.string :as str]
            [integrant.core :as ig]))

(def ^:private auth-header "authorization")

(defn- extract-token
  "Authorization 헤더에서 Bearer 토큰을 추출합니다."
  [request]
  (when-let [auth-header (get-in request [:headers auth-header])]
    (when (str/starts-with? auth-header "Bearer ")
      (subs auth-header 7))))

(defn- unauthorized
  "401 Unauthorized 응답을 반환합니다."
  [message]
  {:status 401
   :headers {"WWW-Authenticate" "Bearer"}
   :body {:error "Unauthorized"
          :message message}})

(defn- forbidden
  "403 Forbidden 응답을 반환합니다."
  [message]
  {:status 403
   :body {:error "Forbidden"
          :message message}})

(defn wrap-auth
  "인증 미들웨어.
   - 요청에서 JWT 토큰을 추출
   - 토큰을 검증
   - 검증된 사용자 정보를 요청에 추가"
  [handler token-gateway]
  (fn [request]
    (if-let [token (extract-token request)]
      (let [result (token/verify token-gateway token)]
        (if (f/failed? result)
          (unauthorized (f/message result))
          (handler (assoc request
                         :identity result
                         :auth-token token))))
      (handler request))))  ;; 토큰이 없는 요청은 그대로 통과

(defn wrap-auth-required
  "특정 핸들러에 대해 인증을 필수로 요구합니다."
  [handler]
  (fn [request]
    (if (:identity request)
      (handler request)
      (unauthorized "Authentication required"))))

(defmethod ig/init-key :web.middleware/auth [_ {:keys [auth-gateway]}]
  {:name ::auth
   :wrap #(wrap-auth % auth-gateway)})

(defmethod ig/init-key :auth/jwt [_ config]
  (let [{:keys [jwt-secret token-expire-hours]} config]
    {:secret jwt-secret
     :expire-hours token-expire-hours}))
