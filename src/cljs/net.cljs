(ns jida.client.net
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [jida.client.utils :as utils])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [cljs.core.match.macros :refer [match]]))

; TODO: Add cljs-ajax?

(defn submit-query [_]
  (let [;query (dommy/value (sel1 :#query-text))
        ;[valid-query? error-offsets] (parens/balanced-parens? query)
        ]
    ;; (if valid-query?
    ;;   (do
    ;;     (dommy/show! (sel1 :#loader))
    ;;     ;; (fm/remote
    ;;     ;;  (query-codeq query) [results]
    ;;     ;;  (display-results! results query)
    ;;     ;;  (dommy/hide! (sel1 "loader"))
    ;;     ;;  (dommy/hide! (sel1 "error-messages")))
    ;;     )
    ;;   (do
    ;;     (ui/display-error! (str "Your parens at offet(s) " (clojure.string/join ", " error-offsets) " aren't properly balanced, please check again"))
    ;;     (ui/select-character (sel1 :#query-text) (first error-offsets))
    ;;     (dommy/hide! (sel1 :#loader))))
    ))



;; (defn queue-import [_]
;;   (let [url (dommy/value (sel1 :#repo-address))]
;;     (if (helper/valid-git-url? url)
;;       (do
;;         (dommy/show! (sel1 :#import-status))
;;         (dommy/set-text! (sel1 :#import-status) "Queueing import")
;;         ;; (fm/remote
;;         ;;  (queue-import url) [_]
;;         ;;  (dommy/set-text! (sel1 "import-status")
;;         ;;               (str "Importing " url "... you may not see it right away.")))
;;         )
;;                                         ; Invalid url
;;       (ui/display-error! "That looks like an invalid Git url. It must start with 'https://'"))))


;; (defn save-query! []
;;   (let [title (js/prompt "Query title")
;;         description (js/prompt "Describe the query")
;;         query-value (dommy/value (sel1 :#query-text))]
;;     ;; (fm/letrem [result (save-query {:query query-value :description description :title title})]
;;     ;;            (update-query-history!)
;;     ;;            result)
;;     ))
