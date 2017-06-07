# edn-diff

A edn diff library for Clojure.

edn-diff currently only supports diffing s-exp and does not preserve forms like vectors or maps (coming soon).

the s-exp differ is clojure port from Michael Weber's Common Lisp implementation and Vincent St-Amour, Felix Winkelmann scheme implementations of SEXP-DIFF.

## Usage

### end-diff.core

The main namespace for edn diff operations in the `edn-diff` library is `edn-diff.core`.

``` clj
(require '[edn-diff.core :as diff])
```
#### diff a s-exp

``` clj
(diff/sexp-diff '(+ 1 (- 1 2 3) (* (+ 1 2) 3))
                '(+ 1 (- 1 2 4) (* 3 3)))
=> ((+ 1 (- 1 2 :old 3 :new 4) (* :old (+ 1 2) :new 3 3)))
```

## Roadmap
* features
  * diff edn obj
    * support s-exp
    * preserve forms like vectors and maps
    * support edn reader extensions
  * 3 way merge
* order of priorities
  * make it work
    * support full edn
    * support reader extensions
  * make it right
    * seperate interfaces in protocols
    * support multiple diff algorithms
      * Zhang-Shasha algorithm
      * A linear tree edit distance algorithm for similar ordered trees
  * make it fast
    * more efficient algorithms
    * transient

## See also..

### Other implementations
* [Michael Weber's Common Lisp implementation](https://github.com/michaelw/mw-diff-sexp)
* [Vincent St-Amour Racket Scheme implementation](https://github.com/stamourv/sexp-diff)

### Papers
* [Zhang-Shasha algorithm](http://www.grantjenks.com/wiki/_media/ideas:simple_fast_algorithms_for_the_editing_distance_between_tree_and_related_problems.pdf)
* [A linear tree edit distance algorithm for similar ordered trees](https://link.springer.com/chapter/10.1007%2F11496656_29)

## License

Copyright © 2017 Simon Levesque

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
