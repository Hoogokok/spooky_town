(ns kit.spooky-town.domain.movie-provider.repository.protocol)

(defprotocol MovieProviderRepository
  (save! [this movie-provider]
    "영화-OTT 플랫폼 연결 정보를 저장합니다.")
  
  (find-by-id [this movie-provider-id]
    "ID로 영화-OTT 플랫폼 연결 정보를 조회합니다.")
  
  (find-by-uuid [this uuid]
    "UUID로 영화-OTT 플랫폼 연결 정보를 조회합니다.")
  
  (find-by-movie [this movie-id]
    "영화 ID로 연결된 OTT 플랫폼들을 조회합니다.")
  
  (find-by-provider [this provider-id]
    "OTT 플랫폼 ID로 연결된 영화들을 조회합니다.")
  
  (delete! [this movie-provider-id]
    "영화-OTT 플랫폼 연결을 삭제합니다.")) 