(ns kit.spooky-town.domain.director.repository.protocol)

(defprotocol DirectorRepository
  (find-by-id [this id] "ID로 감독을 조회합니다.")
  (find-by-ids [this ids] "여러 ID로 감독들을 조회합니다.")
  (save! [this director] "새로운 감독을 저장합니다.")) 