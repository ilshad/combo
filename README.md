# Combo: state/event driven component for Om

[Demo: simple Spreadsheet app (~120 LOC) and Presentation app (~130 LOC)](http://ilshad.com/combo)

## What It Is

Combo is an abstraction layer on top of ClojureScript framework
[Om](http://omcljs.org). It turns state management into single pure
function (we call this function `behavior`) which takes message and
state and returns sequence of messages and new state:


```clojure
(defn behavior [message state]
	...
	[[message-1 message-2 ... message-n] new-state])
```

All computations happen here. Instead of tangled wires of core.async
channels streched between nested components, go-routines, local
states, etc., developers describe entire UI logic in terms of messages
and state machine and they manage it in a single place with unidirectional
flow by pure functions which transform simple data structures:


```
Behavior :: Message, State -> [Message], State
```

Combo provides single entry point, `combo.api/view` which is just Om
component. Also, there are nested Om components, managed by Combo.
We call them `units` and describe them in a DSL-as-data as input for
combo. All required things (see detailed documentation) should be
passed into `opts` for `om/build`:

```clojure
(require '[combo.api :as combo])
...
(om/build combo/view app
	{:opts {:behavior behavior
	        :units [...]})
```

_Combo is not a general-purpose ClojureScript framework._ It is
library for writing _some_ of your components (likely they are rather
complex ones) by _another, special way_: DSL-as-data for
layout and state/event function for behavior. It is intended to be
used within Om-based application along with other, more idiomatic
Om-based code.

## Applicability

However, it is possible to write entire applications with Combo. For
example, [Demo](http://ilshad.com/combo) includes simple
Spreadsheet  and Presentation apps.

Possible use cases for Combo:

- multiple tangled relations between UI widgets
- need for DSL-as-data (maybe, generate DSL for UI from another high-level DSL).

## Main concepts

There are two main ideas in Combo:

- units
- behavior

Units define the structure of the combo. They contain render functions.
Developers write render functions or use basic render functions from
Combo API.

Behavior defines how to manage relations between units.

## Getting started

1. Add `[combo "0.1.1"]` to dependencies.
2. `(require '[combo.api :as combo])`

_(documentation is not accomplished)_

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
