(ns reactive-backplane.elem
  (:refer-clojure :exclude (list get delete)))

(def elems
  "Public singleton atom containing the time-varying state of the
web-service in the form of all key-value pairs that have been PUT."
  (atom {}))

(defn list
  "Dereference elems, producing a clojure map of all key-value pairs
  that have been PUT to the seb service up to the time of this call."
  []
  @elems)

(defn get
  "Get the value corresponding to the id (key) from the web service,
or nil if the key is not present."
  [id]
  (@elems id))

(defn put
  "Put the key-value pair \"id\" \"attrs\" into the web-service
  state."
  [id attrs]
  (let [new-attrs (merge (get id) attrs)]
    (swap! elems assoc id new-attrs)
    new-attrs))

(defn delete
  [id]
  "Delete the key \"id\" and its corresponding value from the
  web-service state."
  (let [old-attrs (get id)]
    (swap! elems dissoc id)
    old-attrs))

(defn clear
  []
  "Clear the entire state of the web-service."
  (reset! elems {})                     ; alternative:
                                        ; (compare-and-set! elems
                                        ; @elems (atom {})
  )