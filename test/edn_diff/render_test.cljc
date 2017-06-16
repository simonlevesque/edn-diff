(ns edn-diff.render-test
  (:require [edn-diff.render :refer :all]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer :all :include-macros true])))

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
