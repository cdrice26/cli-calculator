(ns calculator.core
  (:gen-class)
  (:require [clojure.math :as math]))

(defn tokenize
  "Splits a string into tokens of a mathematical expression"
  [str]
  (re-seq #"[a-z]+|[0-9.]+|[^\s]" str))

(defn parse-float
  "Converts a string to a float"
  [str]
  (try (Float/parseFloat str)
       (catch NumberFormatException _e
         nil)))

(defn numberize
  "Converts all string numbers in a list into numbers"
  [strs]
  (map #(cond (= (parse-float %) nil)
              %
              :else (parse-float %)) strs))

(defn keywordize
  "Converts all operators/functions in a list into keywords"
  [list]
  (map #(cond (= % "+") :add
              (= % "-") :sub
              (= % "*") :mult
              (= % "/") :div
              (= % "%") :mod
              (= % "^") :pow
              (= % "sqrt") :sqrt
              (= % "cbrt") :cbrt
              (= % "log") :log
              (= % "ln") :ln
              (= % "sin") :sin
              (= % "cos") :cos
              (= % "tan") :tan
              (= % "arcsin") :arcsin
              (= % "arccos") :arccos
              (= % "arctan") :arctan
              (= % "pi") 3.1415926535
              (= % "e") 2.7182818284
              :else %)
       list))

(defn is-unary-function
  "Detects whether or not the inputted keyword is a unary function"
  [k]
  (or
   (= k :sqrt)
   (= k :cbrt)
   (= k :log)
   (= k :ln)
   (= k :sin)
   (= k :cos)
   (= k :tan)
   (= k :arcsin)
   (= k :arccos)
   (= k :arctan)))

(defn type-map
  "Returns a list of types for each element of the list (only types used by this program are supported)
   Keywords are returned as themselves"
  [list]
  (map #(cond (or (= (type %) clojure.lang.APersistentVector$SubVector) (= (type %) clojure.lang.PersistentVector)) :vector
              (= (type %) clojure.lang.LazySeq) :list
              (= (type %) clojure.lang.Keyword) %
              (or (= (type %) java.lang.Float) (= (type %) java.lang.Double)) :number)
       list))

(defn add-parentheses-to-mult
  "Add parentheses to implicit multiplication statements"
  [list]
  (flatten [(map (fn [l nl] (cond
                              (and (or (number? l) (= l ")")) (or (= nl "(") (is-unary-function nl))) [l :mult]
                              :else l))
                 list (next list))
            (last list)]))

(defn indexes-of
  "Get all indexes of a value in a seq"
  [e coll]
  (keep-indexed #(if (= e %2) %1 nil) coll))

(defn one-less
  "Returns the item in the list closest to the end that is less than val"
  [list val]
  (if (< (last list) val) (last list) (one-less (drop-last list) val)))

(defn nest
  "Parse parentheses to create nested lists"
  [list]
  ; If there are no parentheses, just return the list
  (if (empty? (indexes-of "(" list))
    list
    ; If there are parentheses, convert the list to a vector
    (let [vec-list (vec list)
          ; This works by getting the innermost closing parenthesis, then finding its corresponding opening parenthesis
          first-closing (first (indexes-of ")" list))
          first-opening (one-less (indexes-of "(" list) first-closing)]
      ; Pull out the terms inside the parentheses, put them in a subvector, and reassemble the main vector
      ; Then call nest on that to evaluate the next pair of parentheses
      (nest (concat
             (subvec vec-list 0 first-opening)
             [(subvec vec-list (+ 1 first-opening) first-closing)]
             (subvec vec-list (+ 1 first-closing)))))))

(defn unchain
  "Makes sure there is no more than one operator per vector"
  [list]
  ; Make a list of the type of teach item in list 
  (let [types (type-map list)
        ; Convert the list into a vector
        vec-list (vec list)]
    ; If there are more than 3 items in a list (too many)
    (if (> (count vec-list) 3)
      ; Follow the order of operations: first, parentheses (which are vectors in this case)
      ; Use map on the list to unchain all vectors in the list, then unchain the finished list in case there are still too many terms
      (cond (some #(= :vector %) types) (unchain (map-indexed #(if (= (nth types %) :vector)
                                                                 (unchain %2)
                                                                 %2) vec-list))
            ; Unchain a unary function call
            ; i is the index of the first keyword representing a unary function
            (some #(is-unary-function %) types) (let [i (first (keep-indexed #(if (is-unary-function %2) %1 nil) types))]
                                                  (-> (concat
                                                       ; List up to the unary function
                                                       (subvec vec-list 0 i)
                                                       ; The unary function and its argument
                                                       [(subvec vec-list i (+ 2 i))]
                                                       ; List after the unary function
                                                       (subvec vec-list (+ 2 i)))
                                                      ; Unchain the next operator
                                                      (unchain)
                                                      ; Convert to vector
                                                      (vec)))
            ; Unchain an exponentiation
            (some #(= % :pow) types) (let [i (first (indexes-of :pow types))]
                                       (-> (concat
                                            (subvec vec-list 0 (- i 1))
                                            [[(nth vec-list (- i 1)) :pow (nth vec-list (+ i 1))]]
                                            (subvec vec-list (+ 2 i)))
                                           (unchain)
                                           (vec)))
            ; Unchain multiplication, division, and modulo
            ; kw is either :mult :div or :mod depending on the operation
            (some #(or (= % :mult) (= % :div) (= % :mod)) types) (let [i (first (keep-indexed #(if (or (= %2 :mult) (= %2 :div) (= %2 :mod)) %1 nil) types))
                                                                       kw (first (keep #(if (or (= % :mult) (= % :div) (= % :mod)) % nil) types))]
                                                                   (-> (concat
                                                                        (subvec vec-list 0 (- i 1))
                                                                        [[(nth vec-list (- i 1)) kw (nth vec-list (+ i 1))]]
                                                                        (subvec vec-list (+ 2 i)))
                                                                       (unchain)
                                                                       (vec)))
            ; Unchain addition and subtraction
            (some #(or (= % :add) (= % :sub)) types) (let [i (first (keep-indexed #(if (or (= %2 :add) (= %2 :sub)) %1 nil) types))
                                                           kw (first (keep #(if (or (= % :add) (= % :sub)) % nil) types))]
                                                       (-> (concat
                                                            (subvec vec-list 0 (- i 1))
                                                            [[(nth vec-list (- i 1)) kw (nth vec-list (+ i 1))]]
                                                            (subvec vec-list (+ 2 i)))
                                                           (unchain)
                                                           (vec)))
            :else vec-list)
      ; If the list is shorter than three terms, all we need to do is unchain any internal vectors
      (map-indexed #(if (= (nth types %) :vector)
                      (-> %2 (unchain) (vec))
                      (if (= (nth types %) :list)
                        (vec %2)
                        %2)) vec-list))))

(defn as-prefix
  "Convert mathematical expressions to prefix notation"
  [list]
  ; If list is not a list but just a number, just return that number
  (if (or (= (type list) clojure.lang.APersistentVector$SubVector) (= (type list) clojure.lang.PersistentVector))
    ; A single term should be a number, just return it as-is
    (cond (<= (count list) 1) (vec list)
        ; Two terms means a unary function and a term, but the term could be a vector so we need to make sure
        ; said vector is also in prefix notation 
          (= (count list) 2) [(nth list 0) (as-prefix (nth list 1))]
        ; Three terms means it's of the form term op term - need to make it op term term and make sure all
        ; terms are also in prefix notation 
          (= (count list) 3) [(nth list 1) (as-prefix (nth list 0)) (as-prefix (nth list 2))])
    list))

(defn evaluate
  "Evaluate a nested vector of math"
  [list]
  (if (or (= (type list) java.lang.Float) (= (type list) java.lang.Double))
    list
    (cond (= (first list) :add) (+ (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :sub) (- (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :mult) (* (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :div) (/ (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :mod) (mod (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :pow) (math/pow (evaluate (nth list 1)) (evaluate (nth list 2)))
          (= (first list) :sqrt) (math/sqrt (evaluate (nth list 1)))
          (= (first list) :cbrt) (math/cbrt (evaluate (nth list 1)))
          (= (first list) :log) (math/log10 (evaluate (nth list 1)))
          (= (first list) :ln) (math/log (evaluate (nth list 1)))
          (= (first list) :sin) (math/sin (evaluate (nth list 1)))
          (= (first list) :cos) (math/cos (evaluate (nth list 1)))
          (= (first list) :tan) (math/tan (evaluate (nth list 1)))
          (= (first list) :arcsin) (math/asin (evaluate (nth list 1)))
          (= (first list) :arccos) (math/acos (evaluate (nth list 1)))
          (= (first list) :arctan) (math/atan (evaluate (nth list 1)))
          :else (first list))))

(defn -main
  [& args]
  (println (-> (first args)
               (tokenize)
               (numberize)
               (keywordize)
               (add-parentheses-to-mult)
               (nest)
               (vec)
               (unchain)
               (vec)
               (as-prefix)
               (evaluate))))
