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
