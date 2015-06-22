# Combo: message driven component for Om

[Demo: simple Spreadsheet app (~120 LOC) and Presentation app (~130 LOC)](http://ilshad.com/combo)

[![Clojars Project](http://clojars.org/combo/latest-version.svg)](http://clojars.org/combo)

## What It Is

Combo is a library for ClojureScript framework [Om](http://omcljs.org).
It turns local state management into function, which takes state and
message and returns new state and a sequence of messages:

```clojure
(defn behavior [state message]
  ...
  [state [message-1 message-2 ...])
```

This state is not managed by Om, it is Combo state. The messages income
from nested components and sent to the nested components, but we do
not write these components. Instead, we describe them in a declarative spec:

```clojure
(def units
  [{:render combo/textarea  :entity :text}
   {:render combo/button    :entity :send}
   {:render my-render-fn    :entity :yet-another-unit]
```

To rule them all, there is single entry point, `combo.api/view`which
is just Om component:

```clojure
(require '[combo.api :as combo])
...
(om/build combo/view app
  {:opts {:behavior behavior
          :units units})
```

_In summary, Combo is a library for writing some of your components by
another, special way: declarative spec for layout and state/event function for
logic. It is intended to be used within Om-based application along
with other Om components._

## Motivation

All significant computations happen in `behavior` function. Instead of
tangled wires of core.async channels streched between nested components,
go-routines, local states, etc., developers describe entire UI logic
in terms of state machine and messages and they manage it in a single
place with unidirectional flow by pure functions which transform
simple data structures:

```
Behavior :: State, Message -> State, [Message]
```

Internally, it is implemented by event bus and pub/sub, and it helps
to control complexity of composite components by keeping the logic
centralized.

## Use cases

Good reasons to adopt Combo are:

- multiple tangled relations between UI widgets
- need for DSL-as-data (maybe, generate DSL for UI from another high-level DSL).

It is possible to write entire applications with Combo. For example,
[Demo](http://ilshad.com/combo) includes simple Spreadsheet and
Presentation apps.

## Main concepts

There are two main ideas in Combo:

- units
- behavior

Units define the structure of the combo. They contain render functions.
Developers write render functions or use basic render functions from
Combo API.

Behavior defines how to manage relations between units.

## Getting started

_(documentation is not accomplished)_

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
