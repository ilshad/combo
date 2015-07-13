# Combo: message driven component for Om

[![Clojars Project](http://clojars.org/combo/latest-version.svg)](http://clojars.org/combo)

[Combo Online: interactive tutorial](http://ilshad.com/combo-online)  (_this is not finished yet_)

[Combo Demo: simple Spreadsheet (~120 LOC) and Presentation Authoring (~130 LOC)](http://ilshad.com/combo)

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
(defn foo [owner spec]
  (let [chan (om/get-state owner :input-chan)]
	(dom/h1 #js {:onMouseOver #(put! chan [(:id spec) :over])}
      (om/get-state owner :title))))
```

The messages sent from render function to behavior through
`input-chan`. It is up to developer of render function to deside what
is the actual format of these messages. Then, you have to build
control flow on these messages in behavior:

```clojure
(defn behavior [state message]
  (core.match/match message
    [:foo :over] [state [[:foo :title "Click here."]]]
	...
	:else [state []]))
```

Outcoming messsages (from behavior) are triplets:

- unit id,
- unit local state key,
- unit local state value.

In other words, instead of writing full components, we have to code
only rendering part and manage state with kind of event hub.

To rule them all, there is an entry point, `combo.api/view`which is
just Om component:

```clojure
(om/build combo/view app
  {:opts {:behavior behavior
          :units units})
```

**In summary, Combo is a library for developing some of your
complex components by another way: stateful functions for rendering
and pure function on messages for state management.**

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

Second advantage is simplier reusability. One may have:

1. a lot of similar widgets in a project,
2. different composite views build with these widgets.

In order to minimize amount of code, one may want:

1. to design a unified way to customize widgets;
2. to design a unified way to build composite views;
3. to describe widgets and composite views by some declarative DSL;
4. flexible but simple solution to describe relations between these widgets;
5. flexible but simple solution to control other problems related to state mangement.

Combo does exactly these things.

## Use cases

Good reasons to adopt Combo are:

- multiple tangled relations between widgets
- similar widgets and different composite views containing them

It is intended to be used within Om-based application along with other
Om components, but also, it is possible to develop apps entirely with Combo.

## Getting started

1. (_this is not finished yet_) Go to
[Combo Online](http://ilshad.com/combo-online),
where you can play with basic concepts and develop some components
without installing anything locally. It is written in Combo itself, so
take a look [its sources](http://github.com/ilshad/combo-online).

2.  (_this is not finished yet_) Start with [tutorial](http://github.com/ilshad/combo/wiki/Tutorial).

3. Take a look at source code of [demo](http://ilshad.com/combo).

Full documentation is [here](http://github.com/ilshad/combo/wiki).

## License

Copyright © 2015 [Ilshad Khabibullin](http://ilshad.com).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
