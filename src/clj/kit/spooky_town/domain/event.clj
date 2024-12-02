(ns kit.spooky-town.domain.event)

(defprotocol EventPublisher
  "도메인 이벤트를 발행하기 위한 프로토콜"
  (publish [this event-type payload]
    "이벤트 타입과 데이터를 받아 이벤트를 발행"))

(defprotocol EventSubscriber
  "도메인 이벤트를 구독하기 위한 프로토콜"
  (subscribe [this event-type handler]
    "이벤트 타입과 핸들러를 등록")) 