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
  https://github.com/michaelw/mw-diff-sexp")

;; structure to track and compare edits
(defstruct edit :type :distance :change)
(defstruct update-edition :type :distance :old :new)

(declare levenshtein-tree-edit)

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
            (conj ss (extend-compound-edit (last ss) (edit-type-fn l))))
          [(empty-compound-edit)]
          lst))

(defn levenshtein-list-row-edit
  "iterate over all the elements of the rows of the levenshtein
  table and return the best and current edit that minimizes the
  distance."
  [new-part {:keys [best current row col]} [old-part row-idx]]
  (let [best-edit (min-edit (extend-compound-edit (get row (inc row-idx))
                                                  (insertion-edit new-part))
                            (extend-compound-edit  current
                                                   (deletion-edit old-part))
                            (extend-compound-edit (get row row-idx)
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
  (let [row (initial-distance deletion-edit old-list)
        col (initial-distance insertion-edit new-list)]
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
    (= old-tree new-tree) (unchanged-edit old-tree)
    (not (and (coll? old-tree)
              (not-empty old-tree)
              (coll? new-tree)
              (not-empty new-tree))) (update-edit old-tree new-tree)
    :else (min-edit
           (update-edit old-tree new-tree)
           (levenshtein-list-edit old-tree new-tree))))

(defn sexp-diff
  "computes a diff between two s-expressions which minimizes the
  number of atoms (datum) in the result tree and marks the differences
  with the :old and :new markers.
  :old is what is being removed and :new is what is being added"
  [old-tree new-tree]
  (render-difference (levenshtein-tree-edit old-tree new-tree)
                     :old :new))
