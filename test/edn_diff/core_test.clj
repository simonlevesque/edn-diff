(ns edn-diff.core-test
  (:require [clojure.test :refer :all]
            [edn-diff.edit :as e]
            [edn-diff.core :refer :all]))

(deftest initial-distance-test
  ;; this test can be converted to a properly based generated test
  (testing "that initial-distance return the distance require to build
  the list"
    (is (= 5
           (count (initial-distance e/unchanged-edit
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
           (initial-distance e/unchanged-edit '(1 2 3))))))

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
