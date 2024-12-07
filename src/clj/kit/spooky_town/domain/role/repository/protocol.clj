(ns kit.spooky-town.domain.role.repository.protocol)

(defprotocol RoleRepository
  (save! [this role] "역할을 저장합니다.")
  (find-by-id [this role-id] "ID로 역할을 조회합니다.")
  (find-by-name [this role-name] "이름으로 역할을 조회합니다.")
  (find-all [this] "모든 역할을 조회합니다.")) 