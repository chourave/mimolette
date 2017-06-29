# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).


## [Unreleased]

## [0.2.1] – 2017-06-29

### Fixed

- The ClojureScript version would require `clojure.test` instead of `cljs.test`
- The Clojure/ClojureScript detection could get confused depending on where
  ClojureScript appears on the classpath

## [0.2.0] – 2017-05-30

### Added

- The `plumula.mimolette.alpha` namespace for interfacing with recent versions
  of spec that use the `alpha` suffix


### Changed

- The unsuffixed `plumula.mimolette` namespace no longer tries to guess whether
  to use the alpha suffix for spec based on the target language. Instead, the
  user should use `plumula.mimolette.alpha` if they use alpha-suffixed spec,
  and `plumula.mimolette` otherwise.


### Removed

- Removed the deprecated Clojure and ClojureScript specific namespaces


## [0.1.0] – 2017-05-18

### Added

- Initiated the change log
- Initiated read me
- Imported `defspec-test` from [Stack Overflow](http://stackoverflow.com/questions/40697841/howto-include-clojure-specd-functions-in-a-test-suite)
- Pushed macros and side-effecting codes to the borders of the system
- Added ClojureScript compatibility
- Made `:opts` portable
 
### Deprecated

- There are Clojure and ClojureScript specific namespaces left from earlier
  development stages. They’re not really doing any harm so I’m leaving them in
  so far.


[Unreleased]: https://github.com/plumula/mimolette/compare/0.2.1...HEAD
[0.2.0]: https://github.com/plumula/mimolette/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/plumula/mimolette/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/plumula/mimolette/compare/init...0.1.0
