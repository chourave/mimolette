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

(set-env! :dependencies '[[plumula/soles "0.4.0" :scope "test"]])
(require '[plumula.soles :refer :all])

(soles! 'plumula/mimolette "0.2.0-SNAPSHOT"
        :dependencies '((:provided
                          [org.clojure/clojure "1.9.0-alpha16"]
                          [org.clojure/clojurescript "1.9.521"])
                         (:compile
                           [org.clojure/test.check "0.9.0"])))

(task-options!
  pom {:description "Run clojure spec tests from clojure test."
       :url         "https://github.com/plumula/mimolette"
       :scm         {:url "https://github.com/plumula/mimolette"}
       :license     {"MIT" "http://www.opensource.org/licenses/mit-license.php"}})
