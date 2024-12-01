(ns kit.spooky-town.infrastructure.session.repository)

(defprotocol SessionRepository
  (create-session [this user-data]
    "세션을 생성하고 세션 ID를 반환합니다.")

  (get-session [this session-id]
    "세션 ID로 사용자 데이터를 조회합니다.")

  (delete-session [this session-id]
    "세션을 삭제합니다.")

  (cleanup-expired-sessions [this]
    "만료된 세션들을 정리합니다.")) 