(ns kit.spooky-town.web.controllers.movie-provider
  (:require [kit.spooky-town.domain.movie-provider.use-case :as movie-provider]
            [failjure.core :as f]))

(defn assign-provider
  [{:keys [parameters movie-provider-use-case]}]
  (let [{:keys [movie-uuid provider-id]} (:body parameters)
        user-uuid (-> parameters :auth :user-uuid)
        result (movie-provider/assign-provider! 
                movie-provider-use-case 
                {:movie-uuid movie-uuid
                 :provider-id provider-id
                 :user-uuid user-uuid})]
    (if (f/failed? result)
      {:status 400
       :body {:error (f/message result)}}
      {:status 201
       :body {:success true}})))

(defn remove-provider
  [{:keys [parameters movie-provider-use-case]}]
  (let [{:keys [movie-uuid provider-id]} (:body parameters)
        user-uuid (-> parameters :auth :user-uuid)
        result (movie-provider/remove-provider! 
                movie-provider-use-case 
                {:movie-uuid movie-uuid
                 :provider-id provider-id
                 :user-uuid user-uuid})]
    (if (f/failed? result)
      {:status 400
       :body {:error (f/message result)}}
      {:status 200
       :body {:success true}})))

(defn get-providers
  [{:keys [parameters movie-provider-use-case]}]
  (let [{:keys [movie-uuid]} (:path parameters)
        providers (movie-provider/get-providers 
                   movie-provider-use-case 
                   {:movie-uuid movie-uuid})]
    {:status 200
     :body {:providers providers}}))

(defn get-movies-by-provider
  [{:keys [parameters movie-provider-use-case]}]
  (let [{:keys [provider-id]} (:path parameters)
        movies (movie-provider/get-movies-by-provider 
                movie-provider-use-case 
                {:provider-id provider-id})]
    {:status 200
     :body {:movies movies}})) 