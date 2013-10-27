(ns jida.support.cors)

(def headers
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Methods" "GET, OPTIONS"
   "Access-Control-Max-Age"       "1000"
   "Access-Control-Allow-Headers" "Content-Type"})
