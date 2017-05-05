; MIT License
;
; Copyright (c) 2017 Frederic Merizen & Timothy Washington
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

(ns plumula.mimolette.clojure
  (:require [clojure.spec.test.alpha :as stest]
            [clojure.spec.alpha :as spec]
            [plumula.mimolette :as mimo]))

(def report-results!
  "A function that takes the result of `stest/check` and reports it to
  `clojure.test`.
  "
  (mimo/reporter stest/abbrev-result spec/explain-out ::stest/val))

(defmacro defspec-test
  "Create a test named `name`, that checks the function(s) named `sym-or-syms`
  against their specs.

  Arguments:
  - `name` a symbol.
  - `sym-or-syms` a fully qualified symbol, or a list of fully qualified symbols.
    The name(s) of the functions to check.
  - `opts` a map of options for `clojure.spec.test.alpha/check`.
    - The `:gen` key is handled by `clojure.spec.test.alpha/check` (see its
      documentation).
    - The contents of the `:opts` key is passed on to
      `clojure.test.check/quick-check` (see its documentation).
    - Special case: inside the `:opts` key, the `:num-tests` key, which is not
      handled by `quick-check`, is the number of checks that will be run for
      each function (defaults to 1000).
  "
  ([name sym-or-syms]
   `(mimo/defspec-test ~name
                       (stest/check ~sym-or-syms)
                       report-results!))
  ([name sym-or-syms opts]
   `(mimo/defspec-test ~name
                       (stest/check ~sym-or-syms ~(mimo/normalise-opts :clojure.spec.test.check/opts opts))
                       report-results!)))
