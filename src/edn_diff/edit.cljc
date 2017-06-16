(ns edn-diff.edit
  "structure to track and compare edits"
  (:require [clojure.spec.alpha :as s]))

(s/def ::edit-spec
  (s/keys :req [::type ::distance (or ::change
                                      (and ::old ::new))]))

(defn edit
  [type distance change]
  {::type type
   ::distance distance
   ::change change})

(defn update-edition
  [distance old new]
  {::type :update
   ::distance distance
   ::old old
   ::new new})

(defn tree-size
  "calculate number of leaf in a tree"
  [tree]
  (if (coll? tree)
    (apply + 1 (map tree-size tree))
    1))

(defn unchanged-edit
  "edit who's distance is the same as the distance of its content."
  [change]
  (edit :unchanged (tree-size change) change))

(defn deletion-edit
  "edit representing the distance of removing its content"
  [change]
  (edit :deletion (inc (tree-size change)) change))

(defn insertion-edit
  "edit representing the distance of adding its content"
  [change]
  (edit :insertion (inc (tree-size change)) change))

(defn update-edit
  "edit representing the distance of removing the old content adding
  the new content"
  [old new]
  (update-edition
   (+ 1 (tree-size old)
      1 (tree-size new))
   old
   new))

(defn compound-edit
  "edit representing the distance of chaining multiple edit together"
  [changes]
  (edit :compound (apply + (map ::distance changes)) changes))

(defn empty-compound-edit
  "edit representing a empty chain of edits.
  note that the distance of '() is 1"
  []
  (compound-edit '()))

(defn extend-compound-edit
  "add edit to a chain of edit"
  [edit-chain edit]
  (compound-edit (cons edit (::change edit-chain))))
