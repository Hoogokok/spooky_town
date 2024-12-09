(ns kit.spooky-town.domain.theater.repository.protocol)

(defprotocol TheaterRepository
  (save! [this theater]
    "극장을 저장합니다.")

  (find-by-id [this theater-id]
    "ULID로 극장을 조회합니다.")

  (find-by-uuid [this uuid]
    "UUID로 극장을 조회합니다.")

  (find-id-by-uuid [this uuid]
    "UUID로 극장의 ULID를 조회합니다.")

  (find-by-chain-type [this chain-type]
    "체인 타입으로 극장들을 조회합니다.")) 