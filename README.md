# ORLY

Built on Om Next, this library publishes a single component, named *Orly*, which optimally packs arbitrary child components inside of itself- You simply pass it these components and it handles the layout. It implements a reasonably fast and reasonably optimal rectangle packing algorithm, using desired relative widths and heights, which you also supply, in the standard Om Next manner.

Benefits of the ORLY and Om Next design:

1. Instead of mucking with the raw DOM, all layout actions of the Orly component are performed as auditable transactions against your app's client state, using the flexible Om Next *query selector* mechanism. This means all layout activity is done in a tidy fashion and under your app's full control.
2. Child components (i.e. the tiles) are just plain React components, defined by your app. They are sent their correct coordinates as needed through the Om Next data propagation mechanism, again through *query selectors* but otherwise can contain completely arbitrary DOM content and styling.

## Usage

In order to use the Orly component, your Om Next app needs to do the following things:

1. Implement a parser read function that passes rectangle data to Orly.
2. Implement a parser mutate function for "app/update-rect" to update the app state when a new rectangle layout is computed by Orly.
3. Watch for an "after-transaction" event in the parent control, to generate additional read transactions that inform the child components (i.e. tiles) of the new layout.
4. Create child components that update their layout in response to the new data.

## Implement parser read function that pass rectangle data to Orly

The Orly component contains the following query selector:

```
[{:rects [:db/id :rect/relwidth :rect/relheight]}]
```

These specify a unique id for each rectangle, and their relative widths and heights. The parent needs to pass props to Orly in this format, as with any Om Next control that specifies a query selector.

## Implement a parser mutate function for "app/update-rect"

When Orly calculates a new layout you need to write this info to your app's state. If you are using Datascript, this function might look as follows:

```
(defmethod mutate 'app/update-rect
           [{:keys [state]} _ rect]
           {:value  []
            :action (fn []
                        (d/transact! state [rect]))})
```

## Watch for an "after-transaction" event in the parent control

Since Orly is generating a mutation transaction from within a library, the parent component has the responsibility to notify other components (especially the child components in the layout) of layout changes. The parent can do this by sending a followup transaction, as follows:

```
(orly (om/computed layout
                   {:after-transaction (fn []
                                           (om/transact! this [{:main [{:rects [:rect/width :rect/height :rect/left :rect/top]}]}]))})
      ...)
```

## Create child components that update their layout in response to the new data

The child components in Orly are simply passed in as children to the control:

```
(orly ...
     (for [rect rects]
          (child rect)))
```

Each child should query for left, top, width, and height in their query selector, and update their position/size accordingly, something like this:

```
(dom/div #js {:style #js {:position "absolute"
                          :width    width
                          :height   height
                          :left     left
                          :top      top}}
         ...)
```

For more details on usage, please see the included example, which you can also view live here: http://lisperati.com/orly

## TODOS

1. This app does not currently support Google Closure's SIMPLE_OPTIMIZATIONS or ADVANCED_OPTIMIZATIONS. The reason for this is still unclear, but should be resolved soon.
2. The way layout transactions are handled seems a bit awkward and will likely be improved in the future.
3. There is a setTimeout call in the Orly component to sidestep a React issue and this is causing some FOUC (Flash of unstyled content).
4. The "grow" feature causes tile overlap on some edge cases that needs to be resolved.
5. My own understanding of Om Next is still incopmplete- This library will probably change in response to feedback from other early Om Next developers.

## License

Copyright © 2015 Conrad Barski

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
