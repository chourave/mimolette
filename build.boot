(set-env! :dependencies [['plumula/soles "0.1.0-SNAPSHOT" :scope "test"]])
(require '[plumula.soles.dependencies :refer [add-dependencies! add-base-dependencies!]])
(add-base-dependencies!)
(require '[plumula.soles :refer :all])

(add-dependencies!
  :compile [org.clojure/test.check
            swiss-arrows])

(soles! 'plumula/mimolette)
