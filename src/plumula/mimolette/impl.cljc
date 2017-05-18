; MIT License
;
; Copyright (c) 2017 Frederic Merizen & Kenny Williams
;
; Permission is hereby granted, free of charge, to any person obtaining a copy
; of this software and associated documentation files (the "Software"), to deal
; in the Software without restriction, including without limitation the rights
; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
; copies of the Software, and to permit persons to whom the Software is
; furnished to do so, subject to the following conditions:
;
; The above copyright notice and this permission notice shall be included in all
; copies or substantial portions of the Software.
;
; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
; SOFTWARE.

(ns plumula.mimolette.impl
  (:require [clojure.string :as string]
            [clojure.test :as test]
            [#?(:clj clojure.spec.test.alpha :cljs cljs.spec.test) :as stest]
            [#?(:clj clojure.spec.alpha :cljs cljs.spec) :as spec]))

(defn success-report
  "Returns a sequence of one message, which is suitable for consumption by
  `test/do-report`, and that reports that the tests corresponding to `results`
  succeeded. Should only be called if all tests did succeed.
  The `results` argument should be the result of `stest/check` where `stest` is
  `clojure.spec.test` (or its alpha or cljs equivalent).
  "
  [results]
  [{:type    :pass
    :message (str "Generative tests pass for "
                  (string/join ", " (map :sym results)))}])

(defn failure-report
  "Returns a sequence of messages, each message suitable for consumption by
  `test/do-report`. The messages describe the test failures in `results`.
  The `results` argument should be the result of `stest/check` where `stest` is
  `clojure.spec.test` (or its alpha or cljs equivalent).
  "
  [results]
  (for [failed-check (filter :failure results)
        :let [r (stest/abbrev-result failed-check)
              failure (:failure r)]]
    {:type     :fail
     :message  (with-out-str (spec/explain-out failure))
     :expected (->> r :spec rest (apply hash-map) :ret)
     :actual   (if (instance? #?(:clj Throwable :cljs js/Error) failure)
                 failure
                 (::stest/val failure))}))

(defn success?
  "Takes the result of `stest/check`, and returns a boolean:
  - true if all checks succeded
  - false otherwise
  "
  [results]
  (every? nil? (map :failure results)))

(defn print-report!
  "Prints a test-report for `results`, formatting them with
  - `report-success` if `success?` is true
  - `report-failure` otherwise
  Returns `success?`
  "
  [report-success report-failure results success?]
  (doseq [:let [reporter (if success? report-success report-failure)
                reports (reporter results)]
          report reports]
    (test/do-report report))
  success?)

(defn report-results!
  "Given a list of `results` from `stest/check`
  - returns true if all checks succeeded, and false otherwise
  - report the results to clojure.test, using `report-success` or
    `report-failure` to format the results depending on whether all tests
    succeeded or some failed

  Arguments:
  - `report-success` and `report-failure` should be functions that take the
    result of `stest/check` and return a list of maps in the format expected
    by `clojure.test/do-report`
  - `results` should be the result of `stest/check`
  "
  [report-success report-failure results]
  (->> results
       success?
       (print-report! report-success report-failure results)))
