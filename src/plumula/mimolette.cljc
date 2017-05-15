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
  (:require [clojure.test :as test]
            [plumula.mimolette.impl :as mimo])
  #?(:cljs (:require-macros plumula.mimolette)))

(defn cljs?
  "True if we are macro-expanding into ClojureScript."
  []
  (boolean (find-ns 'cljs.analyzer)))

(def option-keyword
  "The keyword where `stest/check` will look for options to forward to
  `quick-check`."
  (if (cljs?)
    :clojure.spec.test.check/opts
    :clojure.test.check/opts))

(defn normalise-opts
  "Returns the map `opts`, with the :opts key (if present) replaced with the
  host language specific `option-keyword`.
  "
  [opts]
  (let [stc (:opts opts)]
    (cond-> (dissoc opts :opts)
            stc (assoc option-keyword stc))))

(def check-symbol
  "The name of the host language specific `spec` library's `check` function or macro."
  (if (cljs?)
    `cljs.spec.test/check
    `clojure.spec.test.alpha/check))

(defmacro spec-test
  "Check the function(s) named `sym-or-syms` against their specs.

  Arguments:
  - `sym-or-syms` a fully qualified symbol, or a list of fully qualified symbols.
    The name(s) of the functions to check.
  - `opts` a map of options for `clojure.spec.test.alpha/check`.
    - The `:gen` key is handled by `cljs.spec.test/check` (see its documentation).
    - The contents of the `:opts` key is passed on to
      `clojure.test.check/quick-check` (see its documentation).
    - Special case: inside the `:opts` key, the `:num-tests` key, which is not
      handled by `quick-check`, is the number of checks that will be run for
      each function (defaults to 1000).
  "
  ([sym-or-syms]
   `(->> (~check-symbol ~sym-or-syms)
         (mimo/report-results! mimo/success-report mimo/failure-report)))
  ([sym-or-syms opts]
   (let [opts (normalise-opts opts)]
     `(->> (~check-symbol ~sym-or-syms ~opts)
           (mimo/report-results! mimo/success-report mimo/failure-report)))))

(defmacro defspec-test
  "Create a test named `name`, that checks the function(s) named `sym-or-syms`
  against their specs.

  Arguments:
  - `name` a symbol.
  - `sym-or-syms` a fully qualified symbol, or a list of fully qualified symbols.
    The name(s) of the functions to check.
  - `opts` a map of options for `clojure.spec.test.alpha/check`.
    - The `:gen` key is handled by `cljs.spec.test/check` (see its documentation).
    - The contents of the `:opts` key is passed on to
      `clojure.test.check/quick-check` (see its documentation).
    - Special case: inside the `:opts` key, the `:num-tests` key, which is not
      handled by `quick-check`, is the number of checks that will be run for
      each function (defaults to 1000).
  "
  ([name sym-or-syms]
   `(test/deftest ~name (spec-test ~sym-or-syms)))
  ([name sym-or-syms opts]
   `(test/deftest ~name (spec-test ~sym-or-syms ~opts))))
