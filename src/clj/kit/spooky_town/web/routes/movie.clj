(ns kit.spooky-town.web.routes.movie
  (:require [kit.spooky-town.web.controllers.movie :as movie]
            [kit.spooky-town.web.routes.common :refer [authenticated-route-data]]
            [kit.spooky-town.web.middleware.multipart :refer [wrap-multipart-params]]))

(def movie-routes-data
  ["/movies"
   {:swagger {:tags ["movies"]}}
   
   [""
    (merge authenticated-route-data
           {:post {:summary "Create a new movie"
                  :parameters {:body [:map
                                    [:title string?]
                                    [:description {:optional true} string?]
                                    [:release-info [:map
                                                  [:release-date string?]
                                                  [:release-status {:optional true} keyword?]]]
                                    [:genres {:optional true} set?]
                                    [:runtime {:optional true} pos-int?]
                                    [:actor-ids {:optional true} [:set string?]]
                                    [:director-ids {:optional true} [:set string?]]
                                    [:theater-ids {:optional true} [:set string?]]]}
                  :responses {201 {:body [:map [:movie-uuid string?]]}
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
   
   ["/:movie-uuid"
    (merge authenticated-route-data
           {:get {:summary "Get movie details"
                  :parameters {:path [:map [:movie-uuid string?]]
                             :query [:map
                                    [:include-actors {:optional true} boolean?]
                                    [:include-directors {:optional true} boolean?]
                                    [:include-theaters {:optional true} boolean?]]}
                  :responses {200 {:body [:map
                                        [:movie-uuid string?]
                                        [:title string?]
                                        [:description {:optional true} string?]
                                        [:poster {:optional true} [:map
                                                                 [:url string?]]]
                                        [:release-info [:map
                                                      [:release-date string?]
                                                      [:release-status keyword?]]]
                                        [:genres set?]
                                        [:runtime {:optional true} pos-int?]
                                        [:actors {:optional true} 
                                                [:vector [:map
                                                         [:actor-uuid string?]
                                                         [:name string?]]]]
                                        [:directors {:optional true} 
                                                   [:vector [:map
                                                            [:director-uuid string?]
                                                            [:name string?]]]]
                                        [:theaters {:optional true}
                                                  [:vector [:map
                                                           [:theater-uuid string?]
                                                           [:name string?]
                                                           [:chain-type keyword?]]]]]}
                          404 {:body empty?}}
                  :handler movie/find-movie}
            
            :put {:summary "Update movie"
                  :middleware [wrap-multipart-params]
                  :parameters {:path [:map [:movie-uuid string?]]
                             :multipart [:map
                                       [:poster {:optional true} any?]]
                             :form [:map
                                   [:title {:optional true} string?]
                                   [:description {:optional true} string?]
                                   [:runtime {:optional true} pos-int?]
                                   [:genres {:optional true} set?]
                                   [:release-info {:optional true} [:map
                                                                  [:release-date string?]
                                                                  [:release-status keyword?]]]
                                   [:actor-ids {:optional true} [:set string?]]
                                   [:director-ids {:optional true} [:set string?]]
                                   [:theater-ids {:optional true} [:set string?]]]}
                  :responses {200 {:body [:map [:movie-uuid string?]]}
                             400 {:body [:map [:error string?]]}
                             404 {:body empty?}}
                  :handler movie/update-movie}})]
   
   ["/:movie-uuid/summary"
    {:get {:summary "Get movie summary"
           :parameters {:path [:map [:movie-uuid string?]]}
           :responses {200 {:body [:map
                                 [:movie-uuid string?]
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