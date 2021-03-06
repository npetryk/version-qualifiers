(ns com.rmn.version-qualified.qualifiers.linear-test
  (:require [com.rmn.version-qualified.core :as v]
            [com.rmn.version-qualified.qualifiers.linear :as linear]
            [com.rmn.version-qualified.qualifiers.qualifier-test-utils :as utils])
  (:use clojure.test))



(def known-versions [:V0 :V1 :V2])


;; Example/test usage
(defmacro versioned
  [body]
  (binding [linear/*known-versions* known-versions]
    (v/version-qualified `utils/*user-version* known-versions body)))


;;;;


(deftest linearly-versioned-macro
  (testing "map structure"
    (is (= {:V0 {:title "title"
                 :removed-key "some other data"
                 :switch-key "original data"}
            :V1 {:title "title"
                 :added-key "some data"
                 :only-key "only data"
                 :switch-key "V1 data"}
            :V2 {:title "title"
                 :added-key "some data"
                 :switch-key "V2 data"}}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               {:title "title"
                (added :V1 :added-key) "some data"
                (removed :V1 :removed-key) "some other data"
                (only #{:V1} :only-key) "only data"
                :switch-key (changed "original data"
                                     :V1 "V1 data"
                                     :V2 "V2 data")})))))
  (testing "vector structure"
    (is (= {:V0 [0]
            :V1 [1 "The Sentinel"]
            :V2 [2 "The Sentinel 2"]}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               [(switch {:V0 0 :V1 1 :V2 2})
                (only #{:V1} "The Sentinel")
                (added :V2 "The Sentinel 2")]))))
    (is (= {:V0 [0]
            :V1 [0 1 1]
            :V2 [0 1 1 2 2 2]}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               [0 (only #{:V1 :V2} 1 1) (added :V2 2 2 2)])))))
  (testing "list structure"
    (is (= {:V0 '(0)
            :V1 '(1 "The Sentinel")
            :V2 '(2 "The Sentinel 2")}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               (list (switch {:V0 0 :V1 1 :V2 2})
                     (only #{:V1} "The Sentinel")
                     (added :V2 "The Sentinel 2"))))))
    (is (= {:V0 '(0)
            :V1 '(0 1 1)
            :V2 '(0 1 1 2 2 2)}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               (list (added :V0 0)
                     (only #{:V1 :V2} 1 1)
                     (added :V2 2 2 2)))))))
  (testing "top-level qualifier"
    (is (= {:V0 0
            :V1 1
            :V2 2}
           (utils/eval-qualified [:V0 :V1 :V2]
             (versioned
               (switch {:V0 0 :V1 1 :V2 2})))))))
