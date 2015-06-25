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

The state is not managed by Om and its updates do not cause any re-rendering.

Messages income from nested components and sent to them, but we do
not have to write these components. Instead, we describe them as units in
a declarative spec:

```clojure
(def units
  [{:render combo/textarea  :entity :text}
   {:render combo/button    :entity :send}
   {:render my-render-fn    :entity :foo}])
```

where each unit is based on some render function. In other words,
instead of writing full components, we have to code only part:
render methods.

To rule them all, there is single entry point, `combo.api/view`which
is just Om component:

```clojure
(require '[combo.api :as combo])
...
(om/build combo/view app
  {:opts {:behavior behavior
          :units units})
```

**In summary, Combo is a library for developing some of your
components by another way: declarative spec for rendering and
pure function on messages for state management.**

## Motivation

All significant computations happen in `behavior` function. Instead of
tangled wires of core.async channels streched between nested components,
go-routines, local states and cursors, developers describe entire
logic:

1. In a single place.
2. With pure function.
3. By transforming simple data structures.

Look at this function more closely:

```
Behavior :: State, Message -> State, [Message]
```

If we'd drop second part of the output - `[Message]`, this function
becomes _reducer_ function. So we `reduce` with this function over
stream of input messages. We can save this stream somewhere and
revert state to the past, or replay by reducing over stream from start.

Actually, _it is_ reducing over input messages, with restriction that
part of the result (output messages) is never used in the computations.

**Combo does state management with catamorphism over event stream.**

Internally, it is implemented by event bus and pub/sub. And it helps
to control complexity of composite components by keeping the logic
centralized.

## Use cases

Good reasons to adopt Combo are:

- multiple tangled relations between UI widgets
- need for DSL-as-data (maybe, generate DSL for UI from another high-level DSL).

It is intended to be used within Om-based application along with other
Om components, but also, it is possible to develop apps entirely with Combo.

## Getting started

_(documentation is not accomplished)_

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
