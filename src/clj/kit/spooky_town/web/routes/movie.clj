(ns kit.spooky-town.web.routes.movie
  (:require [kit.spooky-town.web.controllers.movie :as movie] 
            [kit.spooky-town.web.routes.common :refer [authenticated-route-data]]))

(def movie-routes-data
  ["/movies"
   {:swagger {:tags ["movies"]}}
   
   [""
    (merge authenticated-route-data
           {:post {:summary "Create a new movie"
                  :parameters {:body [:map
                                    [:title string?]
                                    [:description {:optional true} string?]
                                    [:release_date string?]]}
                  :responses {201 {:body [:map [:movie-id string?]]}
                             400 {:body [:map [:error string?]]}}
                  :handler movie/create-movie}
            
            :get {:summary "Search movies"
                  :parameters {:query [:map
                                     [:page {:optional true} pos-int?]
                                     [:sort-by {:optional true} keyword?]
                                     [:sort-order {:optional true} keyword?]
                                     [:title {:optional true} string?]
                                     [:director-name {:optional true} string?]
                                     [:actor-name {:optional true} string?]
                                     [:genres {:optional true} set?]
                                     [:release-status {:optional true} keyword?]]}
                  :responses {200 {:body [:map
                                        [:movies vector?]
                                        [:page pos-int?]
                                        [:total-count nat-int?]
                                        [:total-pages pos-int?]]}}
                  :handler movie/search-movies}})]
   
   ["/:movie-id"
    {:get {:summary "Get movie details"
           :parameters {:path [:map [:movie-id string?]]
                       :query [:map
                              [:include-actors {:optional true} boolean?]
                              [:include-directors {:optional true} boolean?]]}
           :responses {200 {:body map?}
                      404 {:body empty?}}
           :handler movie/find-movie}}]
   
   ["/:movie-id/summary"
    {:get {:summary "Get movie summary"
           :parameters {:path [:map [:movie-id string?]]}
           :responses {200 {:body [:map
                                 [:movie-id string?]
                                 [:title string?]
                                 [:poster-url string?]
                                 [:release-date string?]
                                 [:genres set?]
                                 [:director-names {:optional true} vector?]
                                 [:release-status keyword?]]}
                      404 {:body empty?}}
           :handler movie/get-movie-summary}}]])

(defn movie-routes [opts]
  movie-routes-data) 