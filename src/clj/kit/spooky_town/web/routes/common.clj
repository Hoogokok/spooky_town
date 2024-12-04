(ns kit.spooky-town.web.routes.common
  (:require [kit.spooky-town.web.middleware.auth :as auth]))

(def authenticated-route-data
  {:middleware [auth/wrap-auth-required]}) 