# Combo: state/event driven component for Om

[Demo](http://ilshad.com/combo)

Combo is an abstraction layer on top of ClojureScript framework
[Om](http://omcljs.org). It turns state management into single pure
function (we call this function "behavior") which takes message and
state and returns sequence of messages and new state:


```clojure
(defn behavior [message state]
	...
	;; use core.match on message for control flow
	;; and compute new messages and new state
	...
	[[message-1 message-2 ... message-n] new-state])
```

All computations happen here. Instead of tangled wires of core.async
channels streched between nested components, goroutines, local states,
etc, developers describe entire UI logic in terms of messages and state
machine and manage it in a single place with unidirectional flow:

```
Behavior :: Message, State -> [Message], State
```

_Combo is not a general-purpose ClojureScript framework._ It is
library for writing some of your components (likely they are rather
complex ones) by _another, special way_: data-driven DSL for
layout and state/event function for behavior. It is intended to be
used within Om-based application along with other, more idiomatic
Om-based code.

However, it is possible to write entire applications with Combo. For
example, [Demo](http://ilshad.com/combo) includes simple
Spreadsheet (~120 LOC)  and Presentation tool (~130 LOC). Perhaps,
sometimes single Combo component is enaugh to build entire
application and perphaps, sometimes it is pretty expressive code to
describe logic and UI.

Combo provides single entry point, `combo.api/view`, which is just
Om component. All required things (see below) should be passed into
`opts` argument for `om/build`:

```clojure
(require '[combo.api :as combo])
...
(om/build combo/view app
	{:opts {:behavior behavior
		    :layout layout
	        :units [...]})
```

Possible use cases for Combo:

- multiple tangled relations between UI widgets which leads to
multidirectional control flow;

- need for generate UI from data-driven DSL (for example, generate
DSL for UI from another high-level DSL).

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
