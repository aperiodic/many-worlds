# Many Worlds

Many Worlds is a [Quil][quil-url] library that lets you interactively explore many possible paths through the state space of your Quil sketch, in order to incrementally build up cool behavior.
In order to use Many Worlds, the state of your sketch needs to be entirely described by some collection of numbers (the state space position vector),

## Limitations

Currently, Many Worlds has the following limitations on its use:
  * Only works with sketches whose state is entirely described by a vector of numbers and optionally the time elapsed since the beginning of the sketch.
  * Requires Quil 1.7.0.
  * Requires Clojure 1.7.0-beta1 or newer (to fix a classloader bug).
  * Only works with Processing's Java 2D renderer.

## Usage

First, add Many Worlds to your sketch's dependencies:
```clj
[many-worlds "0.1.0"]
```

Many Worlds has two functions you'll need to call in your sketch.
The first is its `setup!` function, which initializes the random bezier walk through your sketch's parameter space, storing the walk's state in the passed atom.
It will also start an instance of the Many Worlds API running in a Jetty server, if given the sufficent number of arguments.
Unsurprisingly, the best place to call Many Worlds's `setup!` function is in your own sketch's setup function.

The second function you'll need to use is the `position-at` function, which you should use in your sketch's draw function.
The `position-at` function takes the atom you gave to the `setup!` function and a time, and returns the current position of the bezier walk i.e. your sketch's current state vector.
This should be the only piece of state that your sketch's draw function uses; if it relies on any other state not managed by Many Worlds, then Many Worlds will not work correctly.

Once you are using these two functions properly, you'll be able to start your sketch and find the Many Worlds control console on `http://localhost:3000`.
From there, you can add the Many Worlds API roots of additional instances of your sketch ("worlds") to the console, and then control them through the console.
You can move through time to evaluate their behavior, select the one with the best behavior, and then move forward to view new diverging behavior between the worlds and repeat the process.

## Further Resources

For a complete example of using Many Worlds in a sketch, consult the `example` directory.
In the example directory, the example can be started by calling `lein run`.

The `setup!` and `position-at` functions have docstrings describing all of their arguments, and the `handler` function in the API namespace describes all of the API endpoints that it serves.

[quil-url]: https://github.com/quil/quil
