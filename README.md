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
and state machine and manage it in a single place with unidirectional
flow, with pure functions which transform simple data structures:


```
Behavior :: Message, State -> [Message], State
```

Combo provides single entry point, `combo.api/view`. We call it `combo
component`. And it is Om/ReactJS component. Also, there are nested Om components,
managed by Combo. We call them `combo units` and we describe them in
a data-DSL as input for combo component. All required things (see detailed
documentation) should be passed into `opts` for `om/build`:

```clojure
(require '[combo.api :as combo])
...
(om/build combo/view app
	{:opts {:behavior behavior
	        :units [...]})
```

_Combo is not a general-purpose ClojureScript framework._ It is
library for writing _some_ of your components (likely they are rather
complex ones) by _another, special way_: data-DSL for
layout and state/event function for behavior. It is intended to be
used within Om-based application along with other, more idiomatic
Om-based code.

## Applicability

However, it is possible to write entire applications with Combo. For
example, [Demo](http://ilshad.com/combo) includes simple
Spreadsheet  and Presentation apps. Perhaps, sometimes even a single
Combo component is enough to build an entire application. Perphaps,
sometimes it is pretty expressive code to describe user interfaces
with complex logic and multi-directional relations between widgets.

Possible use cases for Combo:

- multiple tangled relations between UI widgets

- need for data-DSL (maybe, generate DSL for UI from another high-level DSL).

## Drawbacks

Take a look at the [Demo](http://ilshad.com/combo). In order to create
50 cells, Spreadsheet app mounts 161 Om/React components (50 for
cell containers, 50 for display div, 50 for input field, 10 for rows,
and 1 for table). Each one requires to initialize a couple of
core.async channels, go-routines, etc. The result is some delay while
opening Spreadsheet. We do not write render functions at all. Instead, we are
combining dynamically generating units based on built-in render functions.

As an alternative solution, do not create so many units, but write custom render
functions. We can see an example in Presentation demo app: instead
of dynamically generating unit for each slide thumbnail, we have
defined unit :thumbs and special render function for this. The total
number of units in Presentation app is 18: we create unit for each
button in the toolbar. Possible alternative, for example, is to create
single unit for toolbar with custom render function which will send
buttons-related messages to the behavior.

Actually, there are always options of how to structure Combo
component: more units based on build-in (very basic) render functions
and less units with custom written render functions; define entire
structure by units or define layout to wrap rendering in some
generic way.

## Main concepts

There are two main ideas in Combo:

- units
- behavior

Units define the structure of the component (they are actually nested Om
components, managed by Combo). They contain render functions.

Behavior defines how to manage relations between units.

## Getting started

_(documentation is not accomplished)_

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
