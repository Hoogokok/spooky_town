(ns kit.spooky-town.domain.movie-director.repository.protocol)

(defprotocol MovieDirectorRepository
  (save-movie-director! [this movie-id director-id role]
    "영화-감독 관계 정보를 저장합니다.")
  (find-directors-by-movie [this movie-id]
    "영화 ID로 감독 정보를 조회합니다.")
  (find-movies-by-director [this director-id]
    "감독 ID로 영화 정보를 조회합니다.")
  (delete-by-movie-id! [this movie-id]
    "영화 ID로 감독 정보를 삭제합니다."))
