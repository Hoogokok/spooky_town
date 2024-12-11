(ns kit.spooky-town.domain.movie.test.repository
  (:require [kit.spooky-town.domain.movie.repository.protocol :refer [MovieRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-criteria [_ _]
  (throw (ex-info "Not implemented" {})))

(defn count-by-criteria [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-id-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn mark-as-deleted! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieRepository []
  MovieRepository
  (save! [this movie] (save! this movie))
  (find-by-id [this id] (find-by-id this id))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-by-criteria [this criteria] (find-by-criteria this criteria))
  (count-by-criteria [this criteria] (count-by-criteria this criteria))
  (find-id-by-uuid [this uuid] (find-id-by-uuid this uuid))
 (mark-as-deleted! [this movie-id timestamp] (mark-as-deleted! this movie-id timestamp)))

;; 테스트 헬퍼 함수들
(defn create-test-repository
  ([]
   (create-test-repository {}))
  ([{:keys [save!-fn
            find-by-id-fn
            find-by-uuid-fn
            find-by-criteria-fn
            count-by-criteria-fn
            find-id-by-uuid-fn
            mark-as-deleted!-fn]
     :or {save!-fn (fn [_ _] nil)
          find-by-id-fn (fn [_ _] nil)
          find-by-uuid-fn (fn [_ _] nil)
          find-by-criteria-fn (fn [_ _] [])
          count-by-criteria-fn (fn [_ _] 0)
          find-id-by-uuid-fn (fn [_ _] nil)
          mark-as-deleted!-fn (fn [_ _ _] nil)}}]
   (reify MovieRepository
     (save! [_ movie] (save!-fn movie))
     (find-by-id [_ id] (find-by-id-fn id))
     (find-by-uuid [_ uuid] (find-by-uuid-fn uuid))
     (find-by-criteria [_ criteria] (find-by-criteria-fn criteria))
     (count-by-criteria [_ criteria] (count-by-criteria-fn criteria))
     (find-id-by-uuid [_ uuid] (find-id-by-uuid-fn uuid))
     (mark-as-deleted! [_ movie-id timestamp] (mark-as-deleted!-fn movie-id timestamp))))) 