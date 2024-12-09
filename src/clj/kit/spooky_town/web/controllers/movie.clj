(ns kit.spooky-town.web.controllers.movie
  (:require [kit.spooky-town.domain.movie.use-case :as movie-use-case]
            [kit.spooky-town.domain.movie.query.service :as movie-query]
            [ring.util.http-response :as response]
            [failjure.core :as f]))

(defn create-movie [{:keys [body-params movie-use-case]}]
  (let [result (movie-use-case/create-movie movie-use-case body-params)]
    (if (f/failed? result)
      (response/bad-request {:error (f/message result)})
      (response/created (str "/api/movies/" result) {:movie-id result}))))

(defn find-movie [{:keys [path-params query-params movie-query-service]}]
  (if-let [movie (movie-query/find-movie movie-query-service 
                                        (merge path-params 
                                              (select-keys query-params [:include-actors :include-directors])))]
    (response/ok movie)
    (response/not-found)))

(defn search-movies [{:keys [query-params movie-query-service]}]
  (response/ok (movie-query/search-movies movie-query-service query-params)))

(defn get-movie-summary [{:keys [path-params movie-query-service]}]
  (if-let [movie (movie-query/get-movie-summary movie-query-service path-params)]
    (response/ok movie)
    (response/not-found)))

(defn update-movie [{:keys [path-params multipart-params form-params movie-use-case]}]
  (let [command (cond-> (merge path-params form-params)
                 (:poster multipart-params) (assoc :poster-file (:poster multipart-params)))
        result (movie-use-case/update-movie movie-use-case command)]
    (if (f/failed? result)
      (response/bad-request {:error (f/message result)})
      (response/ok {:movie-id result}))))
 