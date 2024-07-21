(ns calculator.core-test
  (:require [clojure.test :as t]
            [calculator.core :as c]))

(t/deftest test-tokenize
  (t/is (= ["6" "/" "4" "cos" "(" "abs" "(" "-" "3" ")" ")"] (c/tokenize "6/4cos(abs(-3))")))
  (t/is (= ["3" "+" "(" "4" "-" "sin" "(" "2" ")" ")"] (c/tokenize "3+(4-sin(2))"))))

(t/deftest test-parse-float
  (t/is (= 6.0 (c/parse-float "6")))
  (t/is (= 6.0 (c/parse-float "6.0")))
  (t/is (= 6.0 (c/parse-float "6.0 ")))
  (t/is (= 6.0 (c/parse-float " 6.0")))
  (t/is (= 6.0 (c/parse-float " 6.0 ")))
  (t/is (= 6.0 (c/parse-float " 6.0 "))))

(t/deftest test-numberize
  (t/is (= [6.0 "/" 4.0 "cos" "(" "abs" "(" "-" 3.0 ")" ")"] (c/numberize ["6" "/" "4" "cos" "(" "abs" "(" "-" "3" ")" ")"]))))

(t/deftest test-keywordize
  (t/is (= [:add :sub :mult :div] (c/keywordize ["+" "-" "*" "/"])))
  (t/is (= [3 :add 5 :abs 3] (c/keywordize [3 "+" 5 "abs" 3]))))

(t/deftest test-is-unary-function
  (t/is (= true (c/is-unary-function :sqrt)))
  (t/is (= true (c/is-unary-function :cbrt)))
  (t/is (= true (c/is-unary-function :log)))
  (t/is (= true (c/is-unary-function :ln)))
  (t/is (= true (c/is-unary-function :sin)))
  (t/is (= true (c/is-unary-function :cos)))
  (t/is (= true (c/is-unary-function :tan)))
  (t/is (= true (c/is-unary-function :arcsin)))
  (t/is (= true (c/is-unary-function :arccos)))
  (t/is (= true (c/is-unary-function :arctan)))
  (t/is (= true (c/is-unary-function :exp)))
  (t/is (= true (c/is-unary-function :abs)))
  (t/is (= true (c/is-unary-function :floor)))
  (t/is (= true (c/is-unary-function :ceil)))
  (t/is (= true (c/is-unary-function :round)))
  (t/is (= false (c/is-unary-function :add)))
  (t/is (= false (c/is-unary-function :sub)))
  (t/is (= false (c/is-unary-function :mult)))
  (t/is (= false (c/is-unary-function :div)))
  (t/is (= false (c/is-unary-function 3))))

(t/deftest test-type-map
  (t/is (= [:vector :add :number] (c/type-map [[1 2 3] :add 3.0]))))

(t/deftest test-add-keyword-to-implicit-mult
  (t/is (= [3 :mult "("] (c/add-keyword-to-implicit-mult [3 "("])))
  (t/is (= [")" :mult "("] (c/add-keyword-to-implicit-mult [")" "("])))
  (t/is (= [")" :mult :sin] (c/add-keyword-to-implicit-mult [")" :sin]))))

(t/deftest test-indexes-of
  (t/is (= [0 4] (c/indexes-of :add [:add :sub :mult :div :add]))))

(t/deftest test-one-less
  (t/is (= 3 (c/one-less [3 :sub :mult :div 3 5] 5))))

(t/deftest test-nest
  (t/is (= [3 :add [5 :mult 3]] (c/nest [3 :add "(" 5 :mult 3 ")"]))))

(t/deftest test-unchain
  (t/is (= [3 :add [5 :mult [:abs 3]]] (c/unchain [3 :add 5 :mult :abs 3]))))

(t/deftest test-as-prefix
  (t/is (= [:add 3 [:mult 3 5]] (c/as-prefix [3 :add [3 :mult 5]]))))

(t/deftest test-evaluate
  (t/is (= 20.0 (c/evaluate [:add [:sqrt 4.0] [:add 3.0 [:mult 3.0 5.0]]]))))

(t/deftest test-calculate
  (t/is (= 20.0 (c/calculate "sqrt(4) + 3 + 3 * 5"))))
