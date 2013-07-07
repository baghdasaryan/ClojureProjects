(ns mini-kanren.core
  (:refer-clojure :exclude [==])
  (:require [clojure.test :as test])
  (:gen-class)
  (:use [clojure.core.logic]))

(defmacro pdump [x]
  `(let [x# ~x]
     (do (println "----------------")
         (clojure.pprint/pprint '~x)
         (println "~~>")
         (clojure.pprint/pprint x#)
         (println "----------------")
         x#)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))

;;;   ___ _              _             _ 
;;;  / __| |_  __ _ _ __| |_ ___ _ _  / |
;;; | (__| ' \/ _` | '_ \  _/ -_) '_| | |
;;;  \___|_||_\__,_| .__/\__\___|_|   |_|
;;;                |_|                   

(test/deftest foo-test-01-1
  (test/is (= '(true)  (run* [q] (== q true))))
  (test/is (= '(true)  (run* [q] s# (== true q))))
  (test/is (= '()      (run* [q] u# (== true q))))
  (test/is (= '(corn)  (run* [q] s# (== 'corn q))))
  (test/is (= '(false) (run* [q] s# (== false q))))
  (test/is (= '()      (run* [q] (== false 'x))))
  (test/is (= '()      (run* [x] (let [x true] (== false x)))))
  (test/is (= '(_0)    (run* [x] (let [x true] (== true x)))))
  (test/is (= '(true)  (run* [q] (fresh [x] (== true x) (== true q)))))
  (test/is (= '(true)  (run* [q] (fresh [x] (== false x) (== true q)))))
  (test/is (= '(_0)    (run* [x] s#)))
  (test/is (= '(_0)    (run* [x])))
  (test/is (= '(_0)    (run* [x] (let [x false] (fresh [x] (== true x))))))
  (test/is (= '((_0 _1))
              (run* [r] (fresh [x y] (== (lcons x (lcons y ())) r)))))
  (test/is (= '((_0 _1 _0))
              (run* [r] (fresh [x y] (== (lcons x (lcons y (lcons x ()))) r)))))

  ;; Variables are reified "late:" in the order in which they appear
  ;; in "ouput-generating" forms when unifying against variables.
  ;; Here, y gets reified first, then x. They're not reified in the
  ;; order they appear in the 'fresh' declaration.
  (test/is (= '((_0 _1 _0))
              (run* [r] (fresh [x y] (== (lcons y (lcons x (lcons y ()))) r)))))

  (test/is (= '(false)
              (run* [r] (== false r) (== r false))))

  (test/is (= '(true)
              (run* [r] (let [x r] (== true r)))))

  (test/is (= '(_0)
              (run* [r] (fresh [x] (== x r)))))

  (test/is (= '(true)
              (run* [r] (fresh [x] (== true x) (== x r)))))

  (test/is (= '(true)
              (run* [q] (fresh [x] (== x q) (== true q)))))

  (test/is (= '(true)
              (run* [q] (fresh [x] (== x q) (== true x)))))

  (test/is (= '(false) (run* [q] (fresh [x] (== (= x q) q)))))
  (test/is (= '(true)  (run* [q] (fresh [x] (== (= x x) q)))))
  (test/is (= '(true)  (run* [q] (fresh [x] (== (= q q) q)))))

  (test/is (= '(false) (run* [q] (let [x q] (fresh [q] (== x (= x q)))))))
  (test/is (= '(_0)    (run* [q] (let [x q] (fresh [q] (== q (= x q)))))))
  
  (test/is (= 2 (cond false 1 true 2)))

  (test/is (= '(olive oil)
              (run* [q] (conde
                         ((== 'olive q) s#)
                         ((== 'oil   q) s#)
                         (s#            u#)))))

  (test/is (= '(olive)
              (run 1 [q] (conde
                          ((== 'olive q) s#)
                          ((== 'oil   q) s#)
                          (s#            u#)))))
  
  (test/is (= '((split pea))
              (run* [r]
                    (fresh [x y]
                           (== 'split x)
                           (== 'pea   y)
                           (== (lcons x (lcons y ())) r)))))

  (test/is (= '((split pea) (navy bean))
              (run* [r]
                    (fresh [x y]
                           (conde
                            ((== 'split x) (== 'pea   y))
                            ((== 'navy  x) (== 'bean  y))
                            (s#            u#))
                           (== (lcons x (lcons y ())) r)))))
  
  (test/is (= [[:split :pea] [:navy :bean]]
              (run* [r]
                    (fresh [x y]
                           (conde
                            ((== :split x) (== :pea  y))
                            ((== :navy  x) (== :bean y))
                            (s#            u#))
                           (== [x y] r)))))
  
  (test/is (= [[:split :pea :soup] [:navy :bean :soup]]
              (run* [r]
                    (fresh [x y]
                           (conde
                            ((== :split x) (== :pea  y))
                            ((== :navy  x) (== :bean y))
                            (s#            u#))
                           (== [x y :soup] r))))))

(defn teacup [x] (conde
                  ((== 'tea x) s#)
                  ((== 'cup x) s#)
                  (s#          u#)))

(defn teacup2 [x] (conde
                   ((== :tea x) s#)
                   ((== :cup x) s#)
                   (s#          u#)))

(test/deftest foo-test-01-2
  (test/is (= '(tea cup) (run* [x] (teacup x))))

  ;; This next one produces its values in the reverse order predicted
  ;; by the book and by common sense. The top-level conde produces the
  ;; [false true] results (marked A) before producing the nested
  ;; teacup2 results (marked B), no matter the order of A and B in the
  ;; tope-level conde. The wiki explains that clojure.logic's "conde"
  ;; is really the book's "condi", and the order of results is not
  ;; predictable. Clojure.logic does not offer an equivalent to the
  ;; book's "conde".
  
  (test/is (= [[false true] [:tea true] [:cup true]]
              (run* [r]
                    (fresh [x y]
                           (conde
                            ((teacup2 x)  (== true y) s#) ; B
                            ((== false x) (== true y) s#) ; A
                            (s#                       u#))
                           (== [x y] r)))))

  (test/is (= [[false true] [:tea true] [:cup true]]
              (run* [r]
                    (fresh [x y]
                           (conde
                            ((== false x) (== true y) s#) ; A
                            ((teacup2 x)  (== true y) s#) ; B
                            (s#                       u#))
                           (== [x y] r)))))

  ;; We can see this "condi-like" behavior in a simpler case that
  ;; elides the 'fresh':
  (test/is (= [:the-end :tea :cup])
           (run* [x] (conde
                      ((teacup2 x) s#)
                      ((== :the-end x) s#)
                      )))
  
  (test/is (= [:the-end :tea :cup])
           (run* [x] (conde
                      ((== :the-end x) s#)
                      ((teacup2 x) s#)
                      )))

  ;; But, if the last test is a fail, orders are preserved:
  (test/is (= [:tea :cup])
           (run* [x] (conde
                      ((teacup2 x) s#)
                      ((== :the-end x) u#)
                      )))
  
  (test/is (= [:the-end])
           (run* [x] (conde
                      ((== :the-end x) s#)
                      ((teacup2 x) u#)
                      )))

  (test/is (= '([_0 _1] [_0 _1])
              (run* [r]
                    (fresh [x y z]
                           (conde
                            ((== x y) (fresh [x] (== z x)))
                            ((fresh [x] (== y x)) (== z x))
                            (s# u#))
                           (== [y z] r)))))

  (test/is (= '([false _0] [_0 false])
              (run* [r]
                    (fresh [x y z]
                           (conde
                            ((== x y) (fresh [x] (== z x)))
                            ((fresh [x] (== y x)) (== z x))
                            (s# u#))
                           (== [y z] r)
                           (== x false)))))

  (test/is (= [false]
              (run* [q]
                    (let [a (== true q)
                          b (== false q)]
                      b))))

  ;; The folloowing two expressions investigate stopping conditions
  ;; for conde.
  (test/is (= '(true false)
              (run* [q]
                    (conde
                     ((== true q) s#)
                     (s# (== false q))))))
  
  ;; Last clause in a conde gets a default u#:
  (test/is (= '(true false)
              (run* [q]
                    (conde
                     ((== true q) s#)
                     ((== false q) s#)))))

  (test/is (= [false]
              (run* [q]
                    (let [a (== true q) ; This never runs.
                          b (fresh [x]
                                   (== x q)
                                   (== false x))
                          c (conde
                             ((== true q) s#)
                             ((== 42 q))) ; This never runs.
                          ]
                      b))))
  )

;;;   ___ _              _             ___ 
;;;  / __| |_  __ _ _ __| |_ ___ _ _  |_  )
;;; | (__| ' \/ _` | '_ \  _/ -_) '_|  / / 
;;;  \___|_||_\__,_| .__/\__\___|_|   /___|
;;;                |_|

(test/deftest foo-test-02-1
  
  (test/is (= 'c (let [x (fn [a] a)
                       y 'c]
                   (x y))))

  ;; Regular lists, but not quoted when they contain logic variables,
  ;; work in goals:
  (test/is (= '((_0 _1)) (run* [q] (fresh [x y] (== (list x y) q)))))

  ;; Lcons can deliver the values of variables:
  (test/is (= '((_0 _1)) (run* [q] (fresh [x y] (== (lcons x (lcons y ())) q)))))  

  ;; Vectors can get the values of variables out of a goal:
  (test/is (= ['(_0 _1)] (run* [q] (fresh [x y] (== [x y] q)))))

  (test/is (= ['(_0 _1)] (run* [q] (fresh [x y]
                                          (let [u x, v y]
                                            (== [u v] q))))))

  ;; "firsto" works on lists, lcons-lists, and vectors
  (test/is (= '(a)
             (run* [r]
               (firsto (lcons 'a (lcons 'c (lcons 'o (lcons 'r (lcons 'n ()))))) r))))

  (test/is (= '(a) (run* [r] (firsto '(a c o r n) r))))

  (test/is (= '(a) (run* [r] (firsto ['a 'c 'o 'r 'n] r))))

  (test/is (= '(true) (run* [r] (firsto '(a c o r n) 'a) (== true r))))

  ;; No solution to the next one; it fails:
  (test/is (= '() (run* [r] (firsto '(a c o r n) 'z) (== true r))))

  ;; You don't need to use lcons if you're doing internal associations
  (test/is (= '((pear pear _0))
              (run* [r x y]
                    (firsto
                     (lcons r (lcons y ()))
                     x)
                    (== 'pear x))))

  (test/is (= '((pear pear _0))
              (run* [r x y]
                    (firsto
                     (list r y)
                     x)
                    (== 'pear x))))

  (test/is (= '((pear pear _0))
              (run* [r x y]
                    (firsto
                     [r y]
                     x)
                    (== 'pear x))))

  ;; Back to regular lisp for a trice:
  (test/is (= '(grape a) (cons (first '(grape raisin pear))
                               (first '((a) (b) (c))))))

  (test/is (= '((grape a))
              (run* [r]
                    (fresh [x y]
                           (firsto '(grape raisin pear) x)
                           (firsto '((a) (b) (c))       y)
                           ;; LCONS can do improper pairs; that's
                           ;; apparently its main reason for existence:
                           (== r (lcons x y))))))
  
  (test/is (= '(c)
              (run* [r]
                    (fresh [v]
                           (resto '(a c o r n) v)
                           (firsto v r)))))

  (test/is (= '(((raisin pear) a))
              (run* [r]
                    (fresh [x y]
                           (resto '(grape raisin pear) x)
                           (firsto '((a) (b) (c))      y)
                           (== (lcons x  y) r)))))

  (test/is (= [true]
              (run* [q]
                    (resto '(a c o r n) '(c o r n))
                    (== true q))))

  ;; works with vectors, lists, and lcons-trains, again:
  (test/is (= '(o)
              (run* [x]
                    (resto '(c o r n)
                           [x 'r 'n]))))

  (test/is (= '(o)
              (run* [x]
                    (resto '(c o r n)
                           (list x 'r 'n)))))

  (test/is (= '(o)
              (run* [x]
                    (resto '(c o r n)
                           (lcons x (lcons 'r (lcons 'n ())))))))

  ;; This does NOT work with llist: NO SOLUTION TODO understand.
  (test/is (= () ;; '(o)
              (run* [x]
                    (resto '(c o r n)
                           (llist x 'r 'n)))))

  (test/is (= '((a c o r n))
              (run* [l]
                    (fresh [x]
                           (resto l '(c o r n))
                           (firsto l x)
                           (== 'a x)))))
)


