(ns edn-diff.render
  "rendering to transform edits in more readable changes")

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
