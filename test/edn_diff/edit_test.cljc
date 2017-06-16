(ns edn-diff.edit-test
  (:require [edn-diff.edit :refer :all]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer :all :include-macros true])))

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
