(ns mini-kanren.core
  (:use clojure.core.logic)
  (:refer-clojure :exclude [==]))

;;; borrowed from
;;; https://github.com/candera/reasoned-schemer

(defn set=
  "Returns true if a and b have the same elements, regardless of order"
  [a b]
  (= (set a) (set b)))

(defn pair?
  "Returns true if x is a pair-like thing. The slightly awkward
  definition arises out of the mismatch between Scheme and Clojure."
  [x]
  (or (lcons? x) (and (coll? x) (seq x))))

(defn pairo
  "Succeeds if p is a pair-like thing."
  [p]
  (fresh [a d]
    (== (lcons a d) p)))

(defn listo
  "Succeeds if l is a proper list."
  [l]
  (conde
   ((emptyo l) s#)
   ((pairo l) (fresh [d]
                     (resto l d)
                     (listo d)))
   ((s# u#))))

(defn lolo
  "Succeeds if l is a list-of-lists."
  [l]
  (conde
   ((emptyo l) s#)
   ((fresh [a]
           (firsto l a)
           (listo a)) (fresh [d]
                             (resto l d)
                             (lolo d)))
   (s# u#)))

(defn twinso
  "Succeeds if l is a list of identical items."
  [l]
  (fresh [x y]
         (conso x y l)
         (conso x () y)
         ))

;;; Frame 3-36
(defn twinso
  "Succeeds if l is a list of identical items."
  [l]
  (fresh [x]
         (== (list x x) l)
         ))

;;; Frame 3-37
(defn loto
  "Succeeds if l is a list of twins."
  [l]
  (conde
   ((emptyo l) s#)
   ((fresh [a]
            (firsto l a)
            (twinso a)) (fresh [d]
                               (resto l d)
                               (loto d)))
   (s# u#)))

;;; Frame 3-48
(defn listofo
  "Succeeds if predo, applied to every element of l, succeeds."
  [predo l]
  (conde
   ((emptyo l) s#)
   ((fresh [a]
           (firsto l a)
           (predo a)) (fresh [d]
                             (resto l d)
                             (listofo predo d)))
   (s# u#)))

;;; Frame 3-50
(defn loto
  "Succeeds if l is a list of twins."
  [l]
  (listofo twinso l))

;;; Frame 3-54
(defn eq-caro
  "Succeeds if the car of l is x. Identical to 'firsto.'"
  [l x]
  (firsto l x))

;;; Already defined in the core
#_(defn membero
  "Succeeds if x is a member of list l."
  [x l]
  (conde
   ((emptyo l) u#) ; line unnecessary
   ((eq-caro l x) s#)
   (s# (fresh [d]
              (resto l d)
              (membero x d)))))

;;; Frame 3-80
(defn pmembero
  "Succeeds if x is a member of proper list l."
  [x l]
  (conde
   #_((emptyo l) u) ; line unnecessary
   ((eq-caro l x) (resto l ()))
   (s# (fresh [d]
              (resto l d)
              (pmembero x d)))))

;;; Frame 3-83
(defn pmembero
  "Succeeds if x is a member of proper list l, and produces all solutions"
  [x l]
  (conde
   #_((emptyo l) u) ; line unnecessary
   ((eq-caro l x) (resto l ()))
   ((eq-caro l x)  s#)
   (s# (fresh [d]
              (resto l d)
              (pmembero x d)))))

;;; Frame 3-86
(defn pmembero
  "Succeeds if x is a member of proper list l, and produces all unique solutions"
  [x l]
  (conde
   #_((emptyo l) u) ; line unnecessary
   ((eq-caro l x) (resto l ()))
   ((eq-caro l x) (fresh [a d]
                         (resto l (llist a d))))
   (s# (fresh [d]
              (resto l d)
              (pmembero x d)))))


