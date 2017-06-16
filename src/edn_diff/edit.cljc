(ns edn-diff.edit
  "structure to track and compare edits")

(defstruct edit :type :distance :change)
(defstruct update-edition :type :distance :old :new)

(defn tree-size
  "calculate number of leaf in a tree"
  [tree]
  (if (coll? tree)
    (apply + 1 (map tree-size tree))
    1))

(defn unchanged-edit
  "edit who's distance is the same as the distance of its content."
  [change]
  (struct edit :unchanged (tree-size change) change))

(defn deletion-edit
  "edit representing the distance of removing its content"
  [change]
  (struct edit :deletion (inc (tree-size change)) change))

(defn insertion-edit
  "edit representing the distance of adding its content"
  [change]
  (struct edit :insertion (inc (tree-size change)) change))

(defn update-edit
  "edit representing the distance of removing the old content adding
  the new content"
  [old new]
  (struct update-edition
          :update
          (+ 1 (tree-size old)
             1 (tree-size new))
          old
          new))

(defn compound-edit
  "edit representing the distance of chaining multiple edit together"
  [changes]
  (struct edit :compound (apply + (map :distance changes)) changes))

(defn empty-compound-edit
  "edit representing a empty chain of edits.
  note that the distance of '() is 1"
  []
  (compound-edit '()))

(defn extend-compound-edit
  "add edit to a chain of edit"
  [edit-chain edit]
  (compound-edit (cons edit (:change edit-chain))))