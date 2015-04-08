(ns many-worlds.api.dev
  (:require [compojure.core :refer [routes GET ANY]]
            [compojure.route :refer [resources]]
            [many-worlds.api :as api]
            [quil.core :as q]
            [ring.util.response :refer [resource-response]]))

(def dev-handler
  (routes
    (GET "/" [] (resource-response "public/index.html"))
    (resources "")))

(comment
  (api/handler )
  )
