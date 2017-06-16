(ns edn-diff.render-test
  (:require [edn-diff.render :refer :all]
            [edn-diff.edit :as e]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer :all :include-macros true])))

(deftest render-test
  (testing "that s-exp and be rebuilt"
    (is (= '(1)
           (render-difference {::e/type :unchanged
                               ::e/distance 1
                               ::e/change 1}
                              :old
                              :new)))
    (is (= '((1 2 3))
           (render-difference {::e/type :compound
                               ::e/distance 4
                               ::e/change (list {::e/type :unchanged, ::e/distance 1, ::e/change 3}
                                                {::e/type :unchanged, ::e/distance 1, ::e/change 2}
                                                {::e/type :unchanged, ::e/distance 1, ::e/change 1})}
                              :old :new))))
  (testing "that makers are applied to modification"
    (testing "updates"
      (is (= '(:old 1 :new 2)
             (render-difference {::e/type :update ::e/distance 4 ::e/old 1 ::e/new 2}
                                :old
                                :new))))
    (testing "insertions"
      (is (= '(:new 1)
             (render-difference {::e/type :insertion ::e/distance 1 ::e/change 1}
                                :old :new))))
    (testing "deletion"
      (is (= '(:old 1)
             (render-difference {::e/type :deletion ::e/distance 1 ::e/change 1}
                                :old :new))))
    (testing "nested edit"
      (is (= '((:old 1))
             (render-difference {::e/type :compound
                                 ::e/distance 2
                                 ::e/change (list {::e/type :deletion ::e/distance 1 ::e/change 1})}
                                :old :new))))
    (testing "empty list"
      (is (= '(())
             (render-difference {::e/type :unchanged ::e/distance 1 ::e/change '()}
                                :old :new))))))
