(ns kit.spooky-town.web.routes.movie
  (:require [kit.spooky-town.web.controllers.movie :as movie]
            [kit.spooky-town.web.middleware.auth :as auth]))

(def movie-routes
  ["/movies"
   {:swagger {:tags ["movies"]}}
   
   [""
    {:post {:summary "Create a new movie"
            :parameters {:body [:map
                              [:title string?]
                              [:description {:optional true} string?]
                              [:release_date string?]]}
            :responses {201 {:body [:map [:movie-id string?]]}
                       400 {:body [:map [:error string?]]}}
            :middleware [auth/wrap-auth]
            :handler movie/create-movie}}]])

(defn movie-routes [opts]
  (movie-routes opts)) 