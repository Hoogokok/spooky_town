(ns kit.spooky-town.web.middleware.multipart
  (:require [clojure.java.io :as io])
  (:import [javax.imageio ImageIO]))

(defn extract-image-dimensions [file]
  (try
    (with-open [is (io/input-stream (:tempfile file))]
      (when-let [image (ImageIO/read is)]
        {:width (.getWidth image)
         :height (.getHeight image)}))
    (catch Exception _
      nil)))

(defn process-image-file [file]
  (when file
    (let [dimensions (extract-image-dimensions file)]
      (when dimensions
        (merge dimensions
               {:file-name (:filename file)
                :file-type (second (re-find #"image/(.+)" (:content-type file)))
                :file-size (:size file)
                :tempfile (:tempfile file)})))))

(defn wrap-multipart-params [handler]
  (fn [request]
    (if-let [file (get-in request [:multipart-params :poster])]
      (if-let [processed-file (process-image-file file)]
        (handler (assoc-in request [:multipart-params :poster] processed-file))
        (handler request))
      (handler request)))) 