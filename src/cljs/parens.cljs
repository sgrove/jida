(ns jida.client.parens)

; Paren-matching tools
(def parens [ "{" "[" "(" 
              "}" "]" ")" ])

; Turn parens into a hashmap with opening parens as keys, closing as
; values. Also pull out opening and closing parens so they're not
; constantly recalculated.
(def pairs (apply zipmap (partition 3 parens)))
(def opening-parens (first (partition 3 parens)))
(def closing-parens (last (partition 3 parens)))

(defn paren? [char]
  "Returns whether a character is one of the following: ({[]})"
  (some #{char} parens))

(defn paren-type [char]
  "Returns whether a character is an :opening, :closing paren, or nil (not any paren)"
  (when (paren? char)
    (if (some #{char} opening-parens)
      :opening
      :closing)))

(defn acceptable-paren? [stack new]
  "Given a vector STACK of previous *open* [index paren]'s, return whether the new paren is acceptable"
  (= (pairs (last (last stack)))
     new))

(defn balanced-parens? [query]
  "Given a query string, return whether the parans are balanced. Returns a vector, first value is bool balanced, second is list of offsets of unclosed parens"
  (loop [query         query
         stack         []
         error-offsets []
         index         0 ]
    ; Unless we're at the end of the query
    (if (zero? (count query))
      ; Make sure our stack and error offsets are empty, or else
      ; return the error offsets
      (if (and (empty? stack)
               (empty? error-offsets))
        [true []]
        [false (concat (map first stack) error-offsets)])

      ; Still more processing left to do
      (let [char (first query)
            paren (paren? char)
            type (paren-type char)]
        ; Opening parens are always acceptable, just add to the stack and recur
        (cond (= :opening type)
              (recur (rest query) (concat stack [[index char]]) error-offsets (inc index))
              ; If it's a closing paren, check the type and see if it's allows
              (= :closing type)
              (if (acceptable-paren? stack char)
                ; If it's an acceptable closing-paren, pop the last
                ; opening paren off the stack (we just closed it) and
                ; recur
                (recur (rest query) (butlast stack) error-offsets (inc index))
                ; If it's unacceptable, then add the current index to the error offsets and recur
                (recur (rest query) stack (concat error-offsets [index]) (inc index)))
              ; It's not a paren character, just recur like normal
              :else
              (recur (rest query) stack error-offsets (inc index)))))))
