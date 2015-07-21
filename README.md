# Combo: message driven component for Om

[![Clojars Project](http://clojars.org/combo/latest-version.svg)](http://clojars.org/combo)

[Combo Demo: simple Spreadsheet and Presentation Authoring](http://ilshad.com/combo)

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
not have to write these components, as we usually do. Instead, we
describe them as units in a declarative spec:

```clojure
(def units
  [{:id :foo :render foo}
   {:id :bar :render combo/button}
   {:id :baz :render combo/textarea}])
```

where `:render` is a function:

```clojure
(defn foo [{:keys [input! class]}]
  (dom/input #js {:className class}
                  :onChange (fn [e]
				              (let [x (.. % -target -value)]
	                            (input! [:foo x])))))
```

The messages sent from render function to behavior with `:input!`
procedure. It is up to developer of render function to decide what
is the actual format of these messages. They are collected as
input in behavior, so you have to build control flow on them:

```clojure
(defn behavior [state message]
  (core.match/match message
	[:foo x] [state [[:foo :class (if (= x "q") "success" "error")]]]
    ...
	:else [state []]))
```

Outcoming messsages (from behavior) should be triplets:

- unit id,
- unit local state key,
- unit local state value.

In other words, instead of writing full Om components, we have to code
only rendering part and manage state with kind of event hub.

To rule them all, there is an entry point, `combo.api/view`which is
just Om component:

```clojure
(om/build combo/view app
  {:opts {:behavior behavior :units units})
```

**In summary, Combo is a library for developing some of your
complex Om components by another way: render functions for rendering
subcomponents and single function on messages for state management.**

## Motivation

### Single place and pure function

It helps to control complexity of composite components by keeping the logic
centralized: all significant computations happen in `behavior`
function. Instead of tangled wires of core.async channels streched
between nested components, go-routines, local states and cursors,
developers describe entire logic:

1. In a single place.
2. With pure function.
3. By transforming plain data.

### Reusability

We have discovered that render functions is much simplier to reuse and they
are more pluggalbe thing than full components. Actually, Combo API
provides few standard render functions
(see [API Documentation](https://github.com/ilshad/combo/wiki/API)),
and you can see that they are pretty flexible and customizable. Entire
applications can be built with only standard render functions, so
developers write only declarative spec and behavior.

## Use cases

Good reasons to adopt Combo are:

- multiple tangled relations between widgets
- similar widgets and different composite views containing them

It is intended to be used within Om-based application along with other
Om components, but also, it is possible to develop apps entirely with Combo.

## Getting started

1. Start with [Tutorial](http://github.com/ilshad/combo/wiki/Tutorial).
2. Take a look at source code of [Demo](http://ilshad.com/combo).
3. Use [API Documentation](https://github.com/ilshad/combo/wiki/API).

Full documentation is [here](http://github.com/ilshad/combo/wiki).

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
