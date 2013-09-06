(ns maybe.core-test
  (:require [clojure.test :refer :all]
            [maybe.core :refer :all]
            [clojure.algo.monads :refer :all]))

;;; http://onclojure.com/2009/03/05/a-monad-tutorial-for-clojure-programmers-part-1/

(deftest a-test
  (testing "m-bind on maybe"
    (is (=

         (with-monad
           identity-m
           (m-bind 1
                   (fn [a]
                     (m-bind (inc a)
                             (fn [b]
                               (m-result (* a b)))))))
         (domonad identity-m
                  [a 1
                   b (inc a)
                   ] (* a b)))

        "Explicit with-monad produces same result as domonad"

        ))

  (is (= 2 ((fn [x] (domonad maybe-m
                            [a x
                             b (inc a)]
                            (* a b))) 1))
      "Maybe monad produces positive result")

  (is (= nil ((fn [x] (domonad maybe-m
                              [a x
                               b nil]
                              (* a b))) 1))  ; Normally expect
                                        ; NullPointerException here
      "Maybe monad produces negative result on second argument (and avoids NullPointerException")
  
  (is (= nil ((fn [x] (domonad maybe-m
                              [a x
                               b (inc a)]
                              (* a b))) nil))  ; Normally expect
                                        ; NullPointerException here
      "Maybe monad produces negative result on first argument (and avoids NullPointerException")
  
  (is (thrown? NullPointerException (let [a 1
                                          b nil]
                                      (* a b)))
      "Non-monadic computation throws NullPointerException")
  )

(deftest exception-throwing-test
  (testing "exceptions are thrown"
    (is (thrown? ArithmeticException (/ 1 0)))
    (is (thrown-with-msg? ArithmeticException #"Divide by zero" (/ 1 0)))
    ))

(deftest comprehension-test
  (testing "sequence monad and comprehension"
    (is (= (domonad sequence-m
                    [a (range 5)
                     b (range a)]
                    (* a b))
           (for [a (range 5)
                 b (range a)]
             (* a b)))
        "Monadic sequence equals for comprehension")))

(defn- divisible? [n k]
  (= 0 (rem n k)))

(def ^:private not-divisible?
  (complement divisible?))

(defn- divide-out [n k]
  (if (divisible? n k)
    (recur (quot n k) k)
    n))

(defn- check-divisibility-by [k n]
  (let [q (divide-out n k)]
    (if (= q n)
      {:error (str n ": not divisible by " k)}
      q)))

(defn- ugly-small-divisor-test [a2]
  (if (not-divisible? a2 2)
    {:error (str a2 ": not divisible by 2")}
    (let [a3 (quot a2 2)]
      (if (not-divisible? a3 3)
        {:error (str a3 ": not divisible by 3")}
        (let [a5 (quot a3 3)]
          (if (not-divisible? a5 5)
            {:error (str a5 ": not divisible by 5")}
            (let [a7 (quot a5 5)]
              (if (not-divisible? a7 7)
                {:error (str a7 ": not divisible by 7")}
                {:success (str a7 ": divisible by 2, 3, 5, and 7")}
                )
              )
            )
          )
        )
      )
    )
  )

(defn- prettier-small-divisor-test [a2]
  (domonad if-not-error-m
           [a3  (check-divisibility-by 2 a2)
            a5  (check-divisibility-by 3 a3)
            a7  (check-divisibility-by 5 a5)
            a11 (check-divisibility-by 7 a7)
            ]
           a7))

(defn- even-prettier-small-divisor-test [a2]
  (with-monad if-not-error-m
    ((m-chain
      [(partial check-divisibility-by 2)
       (partial check-divisibility-by 3)
       (partial check-divisibility-by 5)
       (partial check-divisibility-by 7)
       ])
     a2)))

(defn- prettiest-small-divisor-test [a2]
  (with-monad if-not-error-m
    ((m-chain
      (vec (map #(partial check-divisibility-by %)
                [2 3 5 7])))
     a2)))

(deftest if-not-error-monad-test
  (testing "the if-not-error-monad"
    (is (=
         (ugly-small-divisor-test 42)
         (prettier-small-divisor-test 42)))
    (is (=
         (ugly-small-divisor-test 42)
         (even-prettier-small-divisor-test 42)))
    (is (=
         (ugly-small-divisor-test 42)
         (prettiest-small-divisor-test 42)))    ))


