# Combo: message driven component for Om

[![Clojars Project](http://clojars.org/combo/latest-version.svg)](http://clojars.org/combo)

[Combo Online: interactive tutorial and in in-browser IDE](http://ilshad.com/combo-online)  (_this is not finished yet_)

[Combo Demo: simple Spreadsheet app (~120 LOC) and Presentation app (~130 LOC)](http://ilshad.com/combo)

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
  [{:entity :text   :render combo/textarea}
   {:entity :send   :render combo/button}
   {:entity :foo    :render render-foo}])
```

where `:entity` is identifier and `:render` is a function:

```clojure
(defn render-foo [owner spec]
  (let [return (om/get-state owner :return-chan)
        entity (:entity spec)]
	(dom/h1 #js {:onMouseOut  #(put! return [entity :out])
		         :onMouseOver #(put! return [entity :over])
				 :onClick     #(put! return [entity :click])}
      (or (om/get-state owner :title) "Take a look."))))
```

The messages sent from render function to behavior through
`return-chan`. It is up to developer of render function to deside what
is the actual format of these messages. In any case, you have to build
control flow on these messages in behavior:

```clojure
(defn behavior [state message]
  (case message
    [:foo :out]   [state [[:foo :title "Take a look."]]]
    [:foo :over]  [state [[:foo :title "Click here."]]]
    [:foo :click] [state [[:foo :title "Thanks!"]]]
                  [state []]))
```

Outcoming messsages (from behavior) are triplets
`[entity attribute value]`, where `entity` is identifier of unit, while
`attribute` and `value` becomes key and value in the local state
of the unit.

In other words, instead of writing full components, we have to code
only rendering part and manage state with kind of event hub.

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

1. a lot of similar widgets in the project,
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
