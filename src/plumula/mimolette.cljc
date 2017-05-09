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

(ns plumula.mimolette
  (:require [clojure.string :as string]
            [clojure.test #?@(:clj (:refer [*load-tests*])) :as test]
    #?(:clj [swiss.arrows :refer [-!>]]
       :cljs [cljs.analyzer :refer [*load-tests*]]))
  #?(:cljs (:require-macros [swiss.arrows :refer [-!>]])))

(defn success-report
  "Returns a sequence of one message. The message is suitable for consumption by
  `test/do-report`, that reports that the tests corresponding to `results`
  succeeded. Should only be called if all tests did succeed.

  `results` should be the result of `stest/check`
  where `stest` is clojure.spec.test (or its alpha or cljs equivalent).
  "
  [results]
  [{:type    :pass
    :message (str "Generative tests pass for "
                  (string/join ", " (map :sym results)))}])

(defn failure-report
  "Returns a sequence of messages, each message suitable for consumption by
  `test/do-report`. The messages describe the test failures in `results`.

  Arguments:
  - `abbrev-result` should be `stest/abbrev-result`
  - `explain-out` should be `spec/explain-out`
  - `getval` should be `::stest/val`
  - `results` should be the result of `stest/check`

  where `stest` is `clojure.spec.test` and `spec` is `clojure.spec` (or their
  alpha or cljs equivalent).
  "
  [abbrev-result explain-out getval results]
  (for [failed-check (filter :failure results)
        :let [r (abbrev-result failed-check)
              failure (:failure r)]]
    {:type     :fail
     :message  (with-out-str (explain-out failure))
     :expected (->> r :spec rest (apply hash-map) :ret)
     :actual   (if (instance? #?(:clj Throwable :cljs js/Error) failure)
                 failure
                 (getval failure))}))

(defn success?
  "Takes the result of `stest/check`, and returns a boolean:
  - true if all checks succeded
  - false otherwise
  "
  [results]
  (every? nil? (map :failure results)))

(defn report-results!
  "Given a list of `results` from `stest/check`
  - returns true if all checks succeeded, and false otherwise
  - report the results to clojure.test, using `report-success` or
    `report-failure` to format the results depending on whether all tests
    succeeded or some failed

  Arguments:
  - `results` should be the result of `stest/check`
  - `report-success` and `report-failure` should be functions that take the
    result of `stest/check` and return a list of maps in the format expected
    by `clojure.test/do-report`
  "
  [report-success report-failure results]
  (letfn [(print-report! [success?]
            (doseq [:let [reporter (if success? report-success report-failure)
                          reports (reporter results)]
                    report reports]
              (test/do-report report)))]
    (-> results
        success?
        (-!> print-report!))))

(defn reporter
  "Returns a function that
  - takes the result of `stest/check`
  - reports that result to clojure.test
  - returns true if all checks succeeded, and false otherwise

  Arguments:
  - `abbrev-result` should be `stest/abbrev-result`
  - `explain-out` should be `spec/explain-out`
  - `getval` should be `::stest/val`

  where `stest` is `clojure.spec.test` and `spec` is `clojure.spec` (or their
  alpha or cljs equivalent).
  "
  [abbrev-result explain-out getval]
  (let [failure-report (partial failure-report abbrev-result explain-out getval)]
    (partial report-results! success-report failure-report)))

(defn normalise-opts
  "Returns the map `opts`, with the :opts key (if present) replaced with
  `opt-kw`.
  "
  [opt-kw opts]
  (let [stc (:opts opts)]
    (cond-> (dissoc opts :opts)
            stc (assoc opt-kw stc))))

(defmacro defspec-test
  "If tests are enabled, create a new `clojure.test` test named `name`.

  Arguments:
  - `name` a symbol
  - `check` the body of the test, a form that returns tests results
  - `report-results` a function that takes the result of `check`, reports it
    to `clojure.test` and returns true (if all tests succeeded) or false
    otherwise.
  "
  [name check report-results]
  (when *load-tests*
    `(def ~(vary-meta name assoc :test `#(~report-results ~check))
       (fn [] (test/test-var (var ~name))))))
