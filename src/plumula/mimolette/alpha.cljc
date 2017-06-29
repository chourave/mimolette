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

(ns plumula.mimolette.alpha
  (:require [plumula.mimolette.impl :as impl]
            [#?(:clj clojure.spec.test.alpha :cljs cljs.spec.test.alpha) :as stest]
            [#?(:clj clojure.spec.alpha :cljs cljs.spec.alpha) :as spec])
  #?(:cljs (:require-macros plumula.mimolette.alpha)))

(def spec-shim
  "Wrap `clojure.spec.alpha` functions in a namespace-independent shim."
  (reify impl/SpecShim
    (abbrev-result [this x] (stest/abbrev-result x))
    (explain-out [this ed] (spec/explain-out ed))
    (failure-val [this failure] (::stest/val failure))))

(defmacro check
  "Check functions against their specs, whether we’re running on Clojure or
  ClojureScript.
  "
  [& args]
  `(impl/if-cljs
     (cljs.spec.test.alpha/check ~@args)
     (clojure.spec.test.alpha/check ~@args)))

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
   `(defspec-test ~name ~sym-or-syms nil))
  ([name sym-or-syms opts]
   `(impl/defspec-test check spec-shim ~name ~sym-or-syms ~opts)))
