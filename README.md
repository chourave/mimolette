# mimolette

[](dependency)
```clojure
[plumula/mimolette "0.2.0"] ;; latest release
```
[](/dependency)

Check your [specs] from `clojure.test` and
`cljs.test`. 

Here we check `a-function` and `another-function` against their specs:
```clj
(ns whatever.my-test
  (:require [whatever.my :refer [a-function another-function]] ; The functions we’re going to test

            [whatever.my.spec]                                 ; The specs for our functions are
                                                               ; in another namespace, load them

            [plumula.mimolette.alpha :refer [defspec-test]]))  ; Use the `alpha` version for recent
                                                               ; releases of spec

(defspec-test test-foo-spec `[a-function another-function])    ; Test `a-function` and `another-function`
                                                               ; againts their respective specs and name
                                                               ; the test `test-foo-spec`
```

[specs]: https://clojure.org/guides/spec


## Supported versions of Clojure(Script)

Mimolette works with Clojure `1.9.0-alpha8` and upwards, and ClojureScript
`1.9.183` and upwards. This is because it relies on the `spec.test/abbrev-result`
function.

For Clojure versions `1.9.alpha-8` to `1.9.alpha-15` and ClojureScript version
`1.9.183` to `1.9.521`, you need to require mimolette’s non-`alpha` namespace.

```clj
(ns whatever.my-test
  (:require [plumula.mimolette :refer [defspec-test]]))
```

Starting from Clojure `1.9.alpha-16` and ClojureScript `1.9.542`, you need to
require mimolette’s `alpha` namespace.

```clj
(ns whatever.my-test
  (:require [plumula.mimolette.alpha :refer [defspec-test]]))
```

This is a consequence of the Clojure team [adding][spec-split] an `alpha` suffix
to spec’s namespaces.
 
[spec-split]: https://clojure.org/community/devchangelog#__a_href_https_groups_google_com_d_msg_clojure_10dbf7w2iqo_ec37tzp5aqaj_1_9_spec_split_a_apr_26_2017


## Usage

In your test namespace, require Mimolette – choosing either the `alpha`- or the
non-`alpha`- namespace depending on your [version](#supported-versions-of-clojurescript)
of Clojure(Script). Mimolette works both for Clojure and ClojureScript.

You’re now ready to test your functions against their specs.


#### Testing functions

```clj
(defspec-test test-foo-spec `[a-function another-function])
```

The functions-to-be-tested must be named by fully-qualified symbols. Often, the
most convenient way of getting them is to use the syntax quote as in the example
above.

#### Testing a single function

You can drop the wrapping collection if you’re only checking a single function:

```clj
(defspec-test test-foo-spec `yet-another-function)
```


#### Testing whole namespaces

You might find the [`enumerate-namespace`][enumerate-namespace] function useful
for checking all the functions of one or more namespaces against their
respective specs.

```clj
(defspec-test test-namespaces (stest/enumerate-namespace 'my.namespace))
(defspec-test test-namespaces (stest/enumerate-namespace '[my.other-namespace
                                                           yet-another.namespace]))
```

(assuming `stest` is an alias for `spec.test`).

[enumerate-namespace]: https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/enumerate-namespace


#### Limiting the number of tests

If your test suite is getting too slow because of spec checks, you might want to
restrict the number of values tested. For instance, this code would test
`my-function` for 10 different inputs.
```clj
(defspec-test test-foo-spec `my-function {:opts {:num-tests 10}})
```

The underlying mechanism is a little convoluted. If you pass a map as third
parameter to `defspec-test`, that parameter will get passed on as the opts to
[`stest/check`][stest-check].

`stest/check` will in turn pass a special parameter on to
[`quick-check`][quick-check]. However, for some crazy reason, that parameter is
named differently in Clojure (`:clojure.spec.test.check/opts`) and ClojureScript
(`:clojure.test.check/opts`).

To remedy this, Mimolette will translate the namespace-less `:opts` key to
whichever key the underlying specs implementation understands.

Finally, the `:num-tests` key adds another round of magic: it isn’t actually
interpreted by `quick-check`. Instead, `stest/check` passes it as the `num-tests`
parameter to `quick-check`.

[quick-check]: https://clojure.github.io/test.check/clojure.test.check.html#var-quick-check
[stest-check]: https://clojure.github.io/clojure/branch-master/clojure.spec-api.html#clojure.spec.test/check



## Troubleshooting

###No tests are generated

`defspec-test` will only generate tests for specs that it knows about, so you
want to make sur that your specs are actually loaded. If your specs reside in
the same namespace as your functions, then that’s already taken care of. If your
specs have a namespace of their own, then make sure to require that from your
tests. This is especially true in ClojureScript, Clojure seems to be more
forgiving here.


## Known limitations

- There is no test suite. Now that side-effect-free functions have been broken
  out of the original macro, they should be easier to test.
- The handling of options is incredibly convoluted and ought to be straightened
  out.


## Project history and acknowledgements

The notable changes to this project are documented in the
[change log](CHANGELOG.md).

I ([Frederic Merizen][frederic-merizen]) am not the original author of this
library.

The macro that evolved to become Mimolette made its first public appearance on
November 4th 2016, when [Kenny Williams][kenny-williams] published his
‘[solution][slack-macro] to integrating `clojure.spec.test/check` with
`clojure.test`’ on the [\#clojure-spec][clojure-spec-channel] channel of the
[clojurians] slack.

He didn’t release the macro as a lib at the time, partly because it wasn’t clear
if the practice it enabled was actually recommended, and partly because he felt
that the core team might be working on something similar.

On November 19th 2016, [Timothy Washington][timothy-washington]
[asked][stack-overflow-macro] on Stack Overflow how to run spec checks from
`clojure.test`. On November 21st he answered his own question with the
`defspec-test` macro from the clojurians slack.

This is were I found the macro. I wanted to use it for my own projects. I’m no
great fan of reuse by cut and paste, and so I decided to give it a home on
Clojars,  added ClojureScript support, and broke the macro down into simpler
components, and thus Mimolette was born.

I would like to thank Kenny and Timothy for their feedback and helping me piece
together this history.

[clojure-spec-channel]: https://clojurians.slack.com/messages/C1B1BB2Q3
[clojurians]: https://clojurians.slack.com
[frederic-merizen]: https://www.linkedin.com/in/fredericmerizen
[kenny-williams]: https://github.com/kennyjwilli
[slack-macro]: https://clojurians.slack.com/files/kenny/F2XV8TRC3/clojure_spec_test___clojure_test.clj
[stack-overflow-macro]: http://stackoverflow.com/questions/40697841/howto-include-clojure-specd-functions-in-a-test-suite 
[timothy-washington]: http://stackoverflow.com/users/375616/nutritioustim


## License

Distributed under the [MIT license](LICENSE.txt).
Copyright &copy; 2017 Frederic Merizen & Kenny Williams.
