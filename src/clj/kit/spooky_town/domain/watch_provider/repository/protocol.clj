(ns kit.spooky-town.domain.watch-provider.repository.protocol)

(defprotocol WatchProviderRepository
  (save! [this watch-provider]
    "OTT 플랫폼 정보를 저장합니다.")

  (find-by-id [this provider-id]
    "ID로 OTT 플랫폼을 조회합니다.")

  (find-by-uuid [this uuid]
    "UUID로 OTT 플랫폼을 조회합니다.")

  (find-by-name [this provider-name]
    "이름으로 OTT 플랫폼을 조회합니다.")

  (find-all [this]
    "모든 OTT 플랫폼을 조회합니다.")

  (delete! [this provider-id]
    "OTT 플랫폼을 삭제합니다.")) 