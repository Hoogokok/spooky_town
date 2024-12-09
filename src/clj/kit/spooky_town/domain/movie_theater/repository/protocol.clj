(ns kit.spooky-town.domain.movie-theater.repository.protocol)

(defprotocol MovieTheaterRepository
  (save-movie-theater! [this movie-id theater-id]
    "영화-극장 관계를 저장합니다.")

  (find-theaters-by-movie [this movie-id]
    "영화 ID로 극장들을 조회합니다. N+1 문제를 피하기 위해 한 번의 쿼리로 처리합니다.")

  (find-theaters-by-movies [this movie-ids]
    "여러 영화 ID로 극장들을 조회합니다. 벌크 조회를 통해 N+1 문제를 방지합니다.")

  (find-movies-by-theater [this theater-id]
    "극장 ID로 영화들을 조회합니다.")

  (delete-movie-theater! [this movie-id theater-id]
    "영화-극장 관계를 삭제합니다.")) 