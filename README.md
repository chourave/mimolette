# mimolette

[](dependency)
```clojure
[plumula/mimolette "0.1.0-SNAPSHOT"] ;; latest release
```
[](/dependency)

Check your [specs](https://clojure.org/guides/spec) from `clojure.test` and
`cljs.test`. 


## Usage

In your test namespace, require Mimolette. For the moment, we need different
includes for Clojure and ClojureScript. I will be able to unify them when
ClojureScript adds the `.alpha` suffix to its namespace.

#### Including Mimolette in Clojure
```clj
(ns whatever.my-test
  (:require [plumula.mimolette.clojure :refer [defspec-test]]))
```

#### Including Mimolette in ClojureScript
```clj
(ns whatever.my-test
  (:require [plumula.mimolette.cljs :refer-macros [defspec-test]]))
```

#### Including Mimolette in mixed-mode files
```clj
(ns whatever.my-test
  (:require
    #?(:clj [plumula.mimolette.clojure :refer [defspec-test]]
       :cljs [plumula.mimolette.cljs :refer-macros [defspec-test]])))
```

### Generating tests for your function
Use Mimolette to create a test named `test-foo-spec`, that will check
`a-function` and `another-functnion` against their specs.

#### Testing functions
```clj
(defspec-test test-foo-spec `[a-function another-function])
```

The functions-to-be-tested must be named by fully-qualified symbols. Often, the
most convenient way of getting them is to use the syntax quote as in the example
above.

#### Testing a single function
It is not necessary to use a collection when checking a single function:

```clj
(defspec-test test-foo-spec `yet-another-function)
```

#### Testing whole namespaces
You might find the [`enumerate-namespace`](https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/enumerate-namespace)
functions useful for checking all the functions of one or more namespaces against their respective specs.

```clj
(defspec-test test-namespaces (stest/enumerate-namespace 'my.namespace))
(defspec-test test-namespaces (stest/enumerate-namespace '[my.other-namespace
                                                           yet-another.namespace]))
```

(assuming `stest` is an alias for `clojure.spec.test.alpha` or `cljs.spec.test`).

#### Limiting the number of tests
If your test suite is getting too slow because of spec checks, you might want to
restrict the number of values tested. For instance, this code would test
`my-function` for 10 different inputs.
```clj
(defspec-test test-foo-spec `my-function {:opts {:num-tests 10}})
```

The underlying mechanism is a little convoluted. If you pass a map as third parameter to
`defspec-test`, that parameter will get passed on as the opts to 
[`stest/check`](https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/check).

`stest/check` will in turn pass a special parameter on to
[`quick-check`](https://clojure.github.io/test.check/clojure.test.check.html#var-quick-check).
However, for some crazy reason, that parameter is named differently in Clojure
(`:clojure.spec.test.check/opts`) and ClojureScript (`:clojure.test.check/opts`).

To remedy this, Mimolette will translate the namespace-less `:opts` key to
whichever key the underlying specs implementation understands.

Finally, the `:num-tests` key adds another round of magic: it isn’t actually
interpreted by `quick-check`. Instead, `stest/check` passes it as the `num-tests`
parameter to `quick-check`.

## Known limitations

- Currently, Clojure and ClojureScript need to require different namespaces. I
  could fix this either with elaborate trickery, or by waiting for ClojureScript
  to add the alpha suffix to its spec namespaces. I’m choosing the latter.
- There is no test suite. Now that side-effect-free functions have been broken
  out of the original macro, they should be easier to test.
- The handling of options is incredibly convoluted and ought to be straightened
  out.
## License

Mimolette started its life on Stack Overflow. Timothy Washington 
[asked](http://stackoverflow.com/questions/40697841/howto-include-clojure-specd-functions-in-a-test-suite)
how to run spec checks from `clojure.test`, and then answered his own question
by writing the `defspec-test` macro.

I (Frederic Merizen) wanted to use it for my own projects. I’m no great fan of
reuse by cut and paste, and so I decided to give it a home on Clojars,  added
ClojureScript support, and broke the macro down into simpler components, and
thus Mimolette was born.

Distributed under the [MIT license](LICENSE.txt).  
Copyright &copy; 2017 [Frederic Merizen](https://www.linkedin.com/in/fredericmerizen/) & [Timothy Washington](http://stackoverflow.com/users/375616/nutritioustim).
