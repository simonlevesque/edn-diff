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
