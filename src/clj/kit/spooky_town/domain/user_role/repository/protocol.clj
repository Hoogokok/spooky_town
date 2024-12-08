(ns kit.spooky-town.domain.user-role.repository.protocol)

(defprotocol UserRoleRepository
  (add-user-role! [this user-id role-id]
    "사용자에게 역할을 추가합니다")
  
  (remove-user-role! [this user-id role-id]
    "사용자의 역할을 제거합니다")
  
  (find-roles-by-user [this user-id]
    "사용자의 모든 역할을 조회합니다")
  
  (find-users-by-role [this role-id]
    "특정 역할을 가진 모든 사용자를 조회합니다")) 