(ns kit.spooky-town.domain.movie.repository.protocol)

(defprotocol MovieRepository
  (save! [this movie]
    "영화를 저장합니다.")

  (find-by-id [this movie-id]
    "ID로 영화를 조회합니다.")

  (find-by-uuid [this uuid]
    "UUID로 영화를 조회합니다.")

  ;; 새로 추가되는 메서드들
  (find-by-criteria [this criteria]
    "주어진 조건으로 영화 목록을 검색합니다.
     criteria: {:page pos-int?
               :sort-by keyword?
               :sort-order keyword?
               :title string?
               :director-name string?
               :actor-name string?
               :genres set?
               :release-status keyword?}")

  (count-by-criteria [this criteria]
    "검색 조건에 맞는 전체 영화 수를 반환합니다."))