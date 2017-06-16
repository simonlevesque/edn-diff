(ns edn-diff.render
  "rendering to transform edits in more readable changes"
  (:require [edn-diff.edit :as e]))

(defmulti render-difference
  "rebuild s-exp from edit and apply change marker to element that
  differs"
  (fn [edits _ _]
    (::e/type edits)))

(defmethod render-difference :unchanged
  [edits old-marker new-marker]
  (list (::e/change edits)))

(defmethod render-difference :deletion
  [edits old-marker new-marker]
  (list old-marker (::e/change edits)))

(defmethod render-difference :insertion
  [edits old-marker new-marker]
  (list new-marker (::e/change edits)))

(defmethod render-difference :update
  [edits old-marker new-marker]
  (list old-marker (::e/old edits)
        new-marker (::e/new edits)))

(defmethod render-difference :compound
  [edits old-marker new-marker]
  (list (reduce (fn [res r]
                  (concat res (render-difference r
                                                 old-marker
                                                 new-marker)))
                '()
                (reverse (::e/change edits)))))
