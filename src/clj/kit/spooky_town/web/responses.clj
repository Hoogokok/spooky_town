(ns kit.spooky-town.web.responses
  (:require [ring.util.http-response :as http-response]))

(defn ok [data]
  (http-response/ok
    {:status "success"
     :data data}))

(defn error 
  ([status message]
   (error status message nil))
  ([status message data]
   (case status
     400 (http-response/bad-request 
           {:status "error"
            :message message
            :data data})
     401 (http-response/unauthorized
           {:status "error"
            :message message
            :data data})
     403 (http-response/forbidden
           {:status "error"
            :message message
            :data data})
     404 (http-response/not-found
           {:status "error"
            :message message
            :data data})
     500 (http-response/internal-server-error
           {:status "error"
            :message message
            :data data})
     ;; default
     (http-response/bad-request
       {:status "error"
        :message "Unknown error"
        :data data}))))