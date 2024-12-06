(ns kit.spooky-town.domain.movie.repository.protocol)

(defprotocol MovieRepository
  (save! [this movie]
    "영화 엔티티를 저장합니다. 성공시 저장된 엔티티를 반환합니다.")
  (find-by-id [this id]
    "ID로 영화를 조회합니다.")
  (find-by-uuid [this uuid]
    "UUID로 영화를 조회합니다."))