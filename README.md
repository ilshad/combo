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
   {:render render-foo      :entity :foo}])
```

where each unit is based on some render function:

```clojure
(defn render-foo [owner spec]
  (let [entity (:entity spec)
        value  (om/get-state owner :value)
		return (om/get-state owner :return-chan)]
	(dom/h1 #js {:onMouseOver #(put! return [entity :over  nil])
                 :onMouseOut  #(put! return [entity :out   nil])
                 :onClick     #(put! return [entity :click nil])}
      (or value "Take a look"))))
```

By convention, messages are triplets `[entity attribute value]`, but the
actual format of a message is defined by its render function.

In any case, it is handy to use core.match to parse messages:

```clojure
(require '[cljs.core.match :refer-macros [match]])
...
(defn behavior [state message]
  (match message
    [:foo :over  _] [state [[:foo :value "Click here"]]]
    [:foo :out   _] [state [[:foo :value "Take a look"]]]
    [:foo :click _] [state [[:foo :value "Thanks!"]]]
	...
    :else           [state []]))
```

In other words, instead of writing full components, we have to code
only rendering part and manage state with message hub.

To rule them all, there is an entry point, `combo.api/view`which is
just Om component:

```clojure
(om/build combo/view app
  {:opts {:behavior behavior
          :units    units})
```

**In summary, Combo is a library for developing some of your
components by another way: declarative spec for rendering and
pure function on messages for state management.**

## Motivation

It helps to control complexity of composite components by keeping the logic
centralized: all significant computations happen in `behavior`
function. Instead of tangled wires of core.async channels streched
between nested components, go-routines, local states and cursors,
developers describe entire logic:

1. In a single place.
2. With pure function.
3. By transforming simple data structures.

## Use cases

Good reasons to adopt Combo are:

- multiple tangled relations between UI widgets
- need for DSL-as-data (maybe, generate DSL for UI from another high-level DSL).

It is intended to be used within Om-based application along with other
Om components, but also, it is possible to develop apps entirely with Combo.

## Getting started

1. Go to [Combo Online](http://ilshad.com/combo-online), where you can
play with basic concepts and develop some components without
installing anything locally. It is written in Combo itself, so take a
look [its sources](http://github.com/ilshad/combo-online).

2. Pay attention to [Demo](http://ilshad.com/combo). There are simple
Spreadsheet app and Presentation app. Learn their source code.

3. Start with [Tutorial](http://github.com/ilshad/combo/wiki/Tutorial).

Full documentation is [here](http://github.com/ilshad/combo/wiki).

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
