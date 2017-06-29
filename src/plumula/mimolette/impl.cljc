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
            [#?(:clj clojure.test :cljs cljs.test) :as test]
    #?(:cljs [cljs.analyzer]))
  #?(:cljs (:require-macros plumula.mimolette.impl)))

(defprotocol SpecShim
  "The functions from spec that we rely on, divorced from their namespace."
  (abbrev-result [this x] "spec.test/abbrev-result")
  (explain-out [this ed] "spec/explain-out")
  (failure-val [this failure] "::spec.test/val"))

(defn success-report
  "Returns a sequence of one message, which is suitable for consumption by
  `test/do-report`, and that reports that the tests corresponding to `results`
  succeeded. Should only be called if all tests did succeed.
  The `results` argument should be the result of `spec.test/check`.
  "
  [results]
  [{:type    :pass
    :message (str "Generative tests pass for "
                  (string/join ", " (map :sym results)))}])

(defn failure-report
  "Returns a sequence of messages, each message suitable for consumption by
  `test/do-report`. The messages describe the test failures in `results`.

  Arguments:
  - `spec-shim` should implement the `SpecShim` protocol
  - `results` should be the result of `spec.test/check`
  "
  [spec-shim results]
  (for [failed-check (filter :failure results)
        :let [r (abbrev-result spec-shim failed-check)
              failure (:failure r)]]
    {:type     :fail
     :message  (with-out-str (explain-out spec-shim failure))
     :expected (->> r :spec rest (apply hash-map) :ret)
     :actual   (if (instance? #?(:clj Throwable :cljs js/Error) failure)
                 failure
                 (failure-val spec-shim failure))}))

(defn success?
  "Takes the result of `spec.test/check`, and returns a boolean:
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
  "Given a list of `results` from `spec.test/check`
  - returns true if all checks succeeded, and false otherwise
  - report the results to clojure.test, using `report-success` or
    `report-failure` to format the results depending on whether all tests
    succeeded or some failed

  Arguments:
  - `report-success` and `report-failure` should be functions that take the
    result of `spec.test/check` and return a list of maps in the format expected
    by `clojure.test/do-report`
  - `results` should be the result of `spec.test/check`
  "
  [report-success report-failure results]
  (->> results
       success?
       (print-report! report-success report-failure results)))

(defn cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs."
  [env]
  (boolean (:ns env)))

(defmacro if-cljs
  "Return then if we are generating cljs code and else for Clojure code.
   https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
  [then else]
  (if (cljs-env? &env) then else))

(defn normalise-opts
  "Returns the map `opts`, with the :opts key (if present) replaced with the
  host language specific `option-keyword`.
  "
  [opts canonical-keyword]
  (let [stc (:opts opts)]
    (cond-> (dissoc opts :opts)
            stc (assoc canonical-keyword stc))))

(defmacro spec-test
  "Check the function(s) named `sym-or-syms` against their specs.

  Arguments:
  - `check` a symbol with the `spec.test/check` macro’s fully qualified name
  - `spec-shim` should implement the `SpecShim` protocol
  - `sym-or-syms` a fully qualified symbol, or a list of fully qualified symbols.
    The name(s) of the functions to check.
  - `opts` a map of options for `clojure.spec.test/check`.
    - The `:gen` key is handled by `cljs.spec.test/check` (see its documentation).
    - The contents of the `:opts` key is passed on to
      `clojure.test.check/quick-check` (see its documentation).
    - Special case: inside the `:opts` key, the `:num-tests` key, which is not
      handled by `quick-check`, is the number of checks that will be run for
      each function (defaults to 1000).
  "
  [check spec-shim sym-or-syms opts]
  `(let [opts# (->> (if-cljs :clojure.test.check/opts :clojure.spec.test.check/opts)
                    (normalise-opts ~opts))]
     (->> (~check ~sym-or-syms opts#)
          (report-results! success-report (partial failure-report ~spec-shim)))))

(defmacro deftest
  ""
  [name & body]
  `(if-cljs
    (cljs.test/deftest ~name ~@body)
    (clojure.test/deftest ~name ~@body)))

(defmacro defspec-test
  "Create a test named `name`, that checks the function(s) named `sym-or-syms`
  against their specs.

  Arguments:
  - `check` a symbol with the `spec.test/check` macro’s fully qualified name
  - `spec-shim` should implement the `SpecShim` protocol
  - `name` a symbol.
  - `sym-or-syms` a fully qualified symbol, or a list of fully qualified symbols.
    The name(s) of the functions to check.
  - `opts` a map of options for `clojure.spec.test/check`.
    - The `:gen` key is handled by `cljs.spec.test/check` (see its documentation).
    - The contents of the `:opts` key is passed on to
      `clojure.test.check/quick-check` (see its documentation).
    - Special case: inside the `:opts` key, the `:num-tests` key, which is not
      handled by `quick-check`, is the number of checks that will be run for
      each function (defaults to 1000).
  "
  [check spec-shim name sym-or-syms opts]
  `(deftest ~name
     (spec-test ~check ~spec-shim ~sym-or-syms ~opts)))
