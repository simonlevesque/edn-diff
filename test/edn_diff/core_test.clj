(ns edn-diff.core-test
  (:require [clojure.test :refer :all]
            [edn-diff.core :refer :all]))

(deftest tree-size-test
  (testing "tree size return correct counts"
    (testing "base elemenents"
      (testing "empty list"
        (is (= 1
               (tree-size '()))))
      (testing "non collection elemenents"
        (is (= 1
               (tree-size 1)))
        (is (= 1
               (tree-size 'test)))
        (is (= 1
               (tree-size :test)))))
    (testing "list structures"
      (is (= 4
             (tree-size '(1 2 3))))
      (testing "nested list structures"
        (is (= 7
               (tree-size '(1 2 3 (4 5)))))))))

(deftest edit-type-test
  (testing "edit types"
    (testing "unchanged"
      (is (= :unchanged
             (:type (unchanged-edit '())))))
    (testing "deletion"
      (is (= :deletion
             (:type (deletion-edit '())))))
    (testing "insertion"
      (is (= :insertion
             (:type (insertion-edit '())))))
    (testing "update"
      (is (= :update
             (:type (update-edit '() '())))))
    (testing "compound (chain edits)"
      (testing "empty chain"
        (is (= :compound
               (:type (compound-edit '()))))
        (is (= :compound
               (:type (empty-compound-edit)))))
      (testing "empty chain"
        (is (= :compound
               (:type (extend-compound-edit :test
                                            (empty-compound-edit)))))))))

(deftest render-test
  (testing "that s-exp and be rebuilt"
    (is (= '(1)
           (render-difference {:type :unchanged
                               :distance 1
                               :change 1}
                              :old
                              :new)))
    (is (= '((1 2 3))
           (render-difference {:type :compound
                               :distance 4
                               :change (list {:type :unchanged, :distance 1, :change 3}
                                             {:type :unchanged, :distance 1, :change 2}
                                             {:type :unchanged, :distance 1, :change 1})}
                              :old :new))))
  (testing "that makers are applied to modification"
    (testing "updates"
      (is (= '(:old 1 :new 2)
             (render-difference {:type :update :distance 4 :old 1 :new 2}
                                :old
                                :new))))
    (testing "insertions"
      (is (= '(:new 1)
             (render-difference {:type :insertion :distance 1 :change 1}
                                :old :new))))
    (testing "deletion"
      (is (= '(:old 1)
             (render-difference {:type :deletion :distance 1 :change 1}
                                :old :new))))
    (testing "nested edit"
      (is (= '((:old 1))
             (render-difference {:type :compound
                                 :distance 2
                                 :change (list {:type :deletion :distance 1 :change 1})}
                                :old :new))))
    (testing "empty list"
      (is (= '(())
             (render-difference {:type :unchanged :distance 1 :change '()}
                                :old :new))))))

(deftest initial-distance-test
  ;; this test can be converted to a properly based generated test
  (testing "that initial-distance return the distance require to build
  the list"
    (is (= 5
           (count (initial-distance unchanged-edit
                                    '(1 2 3 4))))))
  (testing "that initial-distance return a lists of edit"
    (is (= [{:type :compound :distance 0 :change '()}
            {:type :compound
             :distance 1
             :change '({:type :unchanged :distance 1 :change 1})}
            {:type :compound
             :distance 2
             :change
             '({:type :unchanged :distance 1 :change 2}
               {:type :unchanged :distance 1 :change 1})}
            {:type :compound
             :distance 3
             :change
             '({:type :unchanged :distance 1 :change 3}
               {:type :unchanged :distance 1 :change 2}
               {:type :unchanged :distance 1 :change 1})}]
           (initial-distance unchanged-edit '(1 2 3))))))

(deftest sexp-diff-test
  (testing "that diff are valid"
    (testing "base element diffs"
      (testing "datum"
        (testing "no changes"
          (is (= '(1)
                 (sexp-diff 1 1))))
        (testing "element replacement"
          (is (= '(:old 1 :new 2)
                 (sexp-diff 1 2)))))
      (testing "lists"
        (testing "no changes"
          (is (= '(())
                 (sexp-diff '() '()))))
        (testing "datum to list change"
          (is (= '(:old 1 :new (1 2))
                 (sexp-diff 1 '(1 2)))))
        (testing "identical list"
          (is (= '((1 2 3 4))
                 (sexp-diff '(1 2 3 4)
                            '(1 2 3 4)))))
        (testing "list extension"
          (is (= '((1 2 :new 3 :new 4))
                 (sexp-diff '(1 2)
                            '(1 2 3 4)))))
        (testing "list element insertion"
          (is (= '((1 :new 2 3 :new 4))
                 (sexp-diff '(1 3)
                            '(1 2 3 4)))))
        (testing "list element removal"
          (is (= '((1 :old 2 :old 3 4))
                 (sexp-diff '(1 2 3 4)
                            '(1 4)))))
        (testing "mix list element insertion and removal"
          (is (= '((:new 0 1 :old 3 :new 2 :old 4 :new 5))
                 (sexp-diff '(1 3 4)
                            '(0 1 2 5)))))
        (testing "replace list when 2 different"
          (testing "when no element matches"
            (is (= '(:old (1 2 3 4) :new (5 6 7 8))
                   (sexp-diff '(1 2 3 4)
                              '(5 6 7 8)))))
          (testing "when some element matches"
            (is (= '(:old (0 1 2 3 4) :new (0 5 6 7 8))
                   (sexp-diff '(0 1 2 3 4)
                              '(0 5 6 7 8))))))))))

(deftest complex-sexp-diff-test
  (testing "that complex s-exp diff are evaluated properly"
    (testing "nested list are supported"
      (is (= '((1 ((2 :old 3 :new 7)
                   4
                   (5 :old 6 :new 8))))
             (sexp-diff '(1 ((2 3)
                             4
                             (5 6)))
                        '(1 ((2 7)
                             4
                             (5 8)))))))))
