(ns kit.spooky-town.domain.common.id.protocol)

(defprotocol IdGenerator
  (generate-ulid [this] "새로운 ULID를 생성합니다.")) 