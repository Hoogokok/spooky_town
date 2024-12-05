(ns kit.spooky-town.domain.movie-actor.repository.protocol)

(defprotocol MovieActorRepository
  (save-movie-actor! [this movie-id actor-id role]
    "영화-배우 관계 정보를 저장합니다.")
  (find-actors-by-movie [this movie-id]
    "영화 ID로 배우 정보를 조회합니다.")
  (find-movies-by-actor [this actor-id]
    "배우 ID로 영화 정보를 조회합니다.")) 