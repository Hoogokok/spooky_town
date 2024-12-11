(ns kit.spooky-town.domain.auth.authorization.protocol)

(defprotocol UserAuthorization
  (has-permission? [this user-uuid permission]
    "사용자가 특정 권한을 가지고 있는지 확인합니다.
     permission: #{:admin :content-manager}")) 