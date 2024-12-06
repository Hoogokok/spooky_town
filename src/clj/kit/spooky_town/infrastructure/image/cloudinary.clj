(ns kit.spooky-town.infrastructure.image.cloudinary
  (:require [kit.spooky-town.domain.common.image.gateway.protocol :as protocol :refer [ImageUploadGateway]]
            [integrant.core :as ig]
            [failjure.core :as f])
  (:import [com.cloudinary Cloudinary]
           [com.cloudinary.utils ObjectUtils]
           [java.util HashMap Map]
           [java.io File]))

(defrecord CloudinaryImageStorage [cloudinary]
  ImageUploadGateway
  (upload [_ file]
    (f/attempt-all
     [params (doto (HashMap.)
               (.put "resource_type" "image")
               (.put "folder" "movies")
               (.put "use_filename" true)
               (.put "unique_filename" true))
      upload-result (.uploader cloudinary)
      result (.upload upload-result
                      ^File (:tempfile file)
                      ^Map params)]
     {:url (.get result "secure_url")
      :width (-> result (.get "width") int)
      :height (-> result (.get "height") int)}
     (f/when-failed [e]
                    (f/fail :image/upload-failed {:error (.getMessage e)})))))

(defmethod ig/init-key :infrastructure/cloudinary-storage
  [_ {:keys [cloud-name api-key api-secret]}]
  (->CloudinaryImageStorage
   (new Cloudinary (ObjectUtils/asMap
                    "cloud_name" cloud-name
                    "api_key" api-key
                    "api_secret" api-secret)))) 