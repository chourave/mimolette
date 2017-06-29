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

(def +project+ 'plumula/mimolette)
(def +version+ "0.2.1")

(set-env! :dependencies '[[plumula/soles "0.5.0" :scope "test"]])
(require '[plumula.soles :refer [add-dir! add-dependencies! testing deploy-local deploy-snapshot deploy-release]])

(add-dependencies! '(:provided
                      [org.clojure/clojure "1.9.0-alpha8"]
                      [org.clojure/clojurescript "1.9.183"])
                   '(:compile
                      [org.clojure/test.check "0.9.0"])
                   '(:test
                      [adzerk/boot-cljs "2.0.0"]
                      [adzerk/boot-cljs-repl "0.3.3"]
                      [adzerk/boot-reload "0.5.1"]
                      [adzerk/boot-test "1.2.0"]
                      [com.cemerick/piggieback "0.2.1"]
                      [crisptrutski/boot-cljs-test "0.3.0"]
                      [doo "0.1.7"]
                      [org.clojure/tools.nrepl "0.2.12"]
                      [pandeiro/boot-http "0.8.3"]
                      [weasel "0.7.0"]))

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-test :as test]
         '[adzerk.bootlaces :refer [bootlaces!]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs report-errors!]]
         '[boot.lein :as lein]
         '[pandeiro.boot-http :refer [serve]])

(add-dir! :source-paths "src")

(bootlaces! +version+)
(lein/generate)

(task-options!
  cljs { :compiler-options {:infer-externs true}}
  pom {:project     +project+
       :version     +version+
       :description "Run clojure spec tests from clojure test."
       :url         "https://github.com/plumula/mimolette"
       :scm         {:url "https://github.com/plumula/mimolette"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"}}
  serve {:dir "target"}
  test-cljs {:js-env        :node
             :update-fs?    true
             :keep-errors?  true
             :optimizations :simple}
  test/test {:exclude     #"^plumula.mimolette.alpha$"})

(deftask dev
         "Launch Immediate Feedback Development Environment."
         []
         (comp
           (testing)
           (serve)
           (watch)
           (test-cljs)
           (test/test)
           (report-errors!)
           (reload)
           (cljs-repl)
           (cljs)
           (target)))
