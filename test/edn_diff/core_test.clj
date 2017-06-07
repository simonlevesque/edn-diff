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
    ))


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
