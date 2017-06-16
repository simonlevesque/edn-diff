(ns edn-diff.core
  "diffs s-expressions based on Levenshtein-like edit distance.

  Clojure port from Michael Weber's Common Lisp implementation and Vincent
  St-Amour, Felix Winkelmann scheme implementations of SEXP-DIFF.

  sexp-diff computes a diff between two s-expressions which minimizes
  the number of atoms (datum) in the result tree, also counting edit
  conditionals :new, :old.

  reference:

  https://github.com/stamourv/sexp-diff/tree/master/sexp-diff
  http://wiki.call-cc.org/eggref/4/sexp-diff
  https://github.com/michaelw/mw-diff-sexp"
  (:require [edn-diff.edit :as e]
            [edn-diff.render :as r]))


(declare levenshtein-tree-edit)

(defn min-edit
  "select a edit with the minimal distance for a list of edits"
  [& edits]
  (apply min-key :distance edits))

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
            (conj ss (e/extend-compound-edit (last ss) (edit-type-fn l))))
          [(e/empty-compound-edit)]
          lst))

(defn levenshtein-list-row-edit
  "iterate over all the elements of the rows of the levenshtein
  table and return the best and current edit that minimizes the
  distance."
  [new-part {:keys [best current row col]} [old-part row-idx]]
  (let [best-edit (min-edit (e/extend-compound-edit (get row (inc row-idx))
                                                    (e/insertion-edit new-part))
                            (e/extend-compound-edit  current
                                                     (e/deletion-edit old-part))
                            (e/extend-compound-edit (get row row-idx)
                                                    (levenshtein-tree-edit old-part new-part)))]
    {:row (assoc row row-idx current)
     :current best-edit
     :best best-edit
     :col col}))

(defn levenshtein-list-col-edit
  "iterate over all the element of the columns of the levenshtein
  table tracking the best of each row of the levenshtein."
  [old-tree {:keys [best row col]} [new-part current]]
  (let [pos-map (reduce (partial levenshtein-list-row-edit new-part)
                        {:best best
                         :current current
                         :row row
                         :col col}
                        (map vector old-tree (range)))]
    (update pos-map :row assoc (dec (count row)) (:best pos-map))))

(defn levenshtein-list-edit
  "compare the old and new list sequentially starting from the first
  element of the old list and return the best edit chain.

  note the old list of columns of the levenshtein table
  and the new list id the rows of the levenshtein table"
  [old-list new-list]
  (let [row (initial-distance e/deletion-edit old-list)
        col (initial-distance e/insertion-edit new-list)]
    (-> (reduce (partial levenshtein-list-col-edit old-list)
                {:best false
                 :row row
                 :col col}
                (map vector new-list (drop 1 col)))
        :best)))

(defn levenshtein-tree-edit
  "work in conjunction with levenshtein-list-edit and
  levenshtein-list-col-edit to recursivly represent the difference
  between the two list as a smallest tree of edits."
  [old-tree new-tree]
  (cond
    (= old-tree new-tree) (e/unchanged-edit old-tree)
    (not (and (coll? old-tree)
              (not-empty old-tree)
              (coll? new-tree)
              (not-empty new-tree))) (e/update-edit old-tree new-tree)
    :else (min-edit
           (e/update-edit old-tree new-tree)
           (levenshtein-list-edit old-tree new-tree))))

(defn sexp-diff
  "computes a diff between two s-expressions which minimizes the
  number of atoms (datum) in the result tree and marks the differences
  with the :old and :new markers.
  :old is what is being removed and :new is what is being added"
  [old-tree new-tree]
  (r/render-difference (levenshtein-tree-edit old-tree new-tree)
                       :old :new))
