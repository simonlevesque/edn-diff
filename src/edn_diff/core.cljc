(ns edn-diff.core
  "diffs s-expressions based on Levenshtein-like edit distance.

  Clojure port from Michael Weber's Common Lisp implementation and Vincent
  St-Amour, Felix Winkelmann scheme implementations of SEXP-DIFF.

  sexp-diff computes a diff between two s-expressions which minimizes
  the number of atoms (datum) in the result tree, also counting edit
  conditionals :new, :old.")

;; structure to track and compare edits
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

(defmulti render-difference
  "rebuild s-exp from edit and apply change marker to element that
  differs"
  (fn [edits _ _]
    (:type edits)))

(defmethod render-difference :unchanged
  [edits old-marker new-marker]
  (list (:change edits)))

(defmethod render-difference :deletion
  [edits old-marker new-marker]
  (list old-marker (:change edits)))

(defmethod render-difference :insertion
  [edits old-marker new-marker]
  (list new-marker (:change edits)))

(defmethod render-difference :update
  [edits old-marker new-marker]
  (list old-marker (:old edits)
        new-marker (:new edits)))

(defmethod render-difference :compound
  [edits old-marker new-marker]
  (list (reduce (fn [res r]
                  (concat res (render-difference r
                                                 old-marker
                                                 new-marker)))
                '()
                (reverse (:change edits)))))

(defn extend-compound-edit
  "add edit to a chain of edit"
  [edit-chain edit]
  (compound-edit (cons edit (:change edit-chain))))

(defn initial-distance
  "for a list return the edits representing the distance for building
  that list progressively.

  return a vector of sub-lists of lst represented in edit form by
  applying edit-type-fn of every element. the first element of the
  vector is a empty edit chain and the subsequent element of the
  vector are sub-list of lst starting from the start of lst.

  e.g

  (initial-distance unchanged-edit '(1 2 3))

  =>

  [{:type :compound, :distance 0, :change ()}
   {:type :compound,
    :distance 1,
    :change ({:type :unchanged, :distance 1, :change 1})}
   {:type :compound,
    :distance 2,
    :change
     ({:type :unchanged, :distance 1, :change 2}
      {:type :unchanged, :distance 1, :change 1})}
   {:type :compound,
    :distance 3,
    :change
     ({:type :unchanged, :distance 1, :change 3}
      {:type :unchanged, :distance 1, :change 2}
      {:type :unchanged, :distance 1, :change 1})}]"
  [edit-type-fn lst]
  (reduce (fn [ss l]
            (conj ss (extend-compound-edit (last ss) (edit-type-fn l))))
          [(empty-compound-edit)]
          lst))
