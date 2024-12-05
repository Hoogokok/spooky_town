(ns kit.spooky-town.web.controllers.movie
  (:require [kit.spooky-town.domain.movie.use-case :as movie-use-case]
            [ring.util.http-response :as response]
            [failjure.core :as f]))

(defn create-movie [{:keys [body-params movie-use-case]}]
  (let [result (movie-use-case/create-movie movie-use-case body-params)]
    (if (f/failed? result)
      (response/bad-request {:error (f/message result)})
      (response/created (str "/api/movies/" result) {:movie-id result})))) 