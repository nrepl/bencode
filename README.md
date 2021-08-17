[![CircleCI](https://circleci.com/gh/nrepl/bencode/tree/master.svg?style=svg)](https://circleci.com/gh/nrepl/bencode/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/nrepl/bencode.svg)](https://clojars.org/nrepl/bencode)
[![cljdoc badge](https://cljdoc.org/badge/nrepl/bencode)](https://cljdoc.org/d/nrepl/bencode/CURRENT)
[![downloads badge](https://versions.deps.co/nrepl/bencode/downloads.svg)](https://clojars.org/nrepl/bencode)

# bencode

A netstring and bencode implementation for Clojure.

This particular implementation was extracted from [nREPL][], so it
could be reused by other applications and be made
ClojureScript-compatible as well.

[nREPL]: https://nrepl.org

There are other netstring/bencode libraries for Clojure out there, but this one has
the distinct advantage that it's certainly going to work well with nREPL. ;-)

## Motivation

In each and every application, which contacts peer processes via some
communication channel, the handling of the communication channel is
obviously a central part of the application. Unfortunately introduces
handling of buffers of varying sizes often bugs in form of buffer
overflows and similar.

A strong factor in this situation is of course the protocol which goes
over the wire. Depending on its design it might be difficult to estimate
the size of the input up front. This introduces more handling of message
buffers to accomodate for inputs of varying sizes. This is particularly
difficult in languages like C, where there is no bounds checking of array
accesses and where errors might go unnoticed for considerable amount of
time.

To address these issues D. Bernstein developed the so called
[netstrings][net]. They are especially designed to allow easy construction
of the message buffers, easy and robust parsing.

BitTorrent extended this to the [bencode][bc] protocol which also
includes ways to encode numbers and collections like lists or maps.

*wire* is based on these ideas.

[net]: http://cr.yp.to/proto/netstrings.txt
[bc]:  http://wiki.theory.org/BitTorrentSpecification#Bencoding

## Usage

Just add `bencode` as a dependency to your project and start hacking.

```clojure
[nrepl/bencode "1.1.0"]
```

The API is documented in great detail [here](https://github.com/nrepl/bencode/blob/master/src/bencode/core.clj).

The main functions in the API are:

* `read-netsring`
* `write-nestring`
* `read-bencode`
* `write-bencode`

Here are some usage examples for each of the functions available:

- `read-netstring`

 ```clojure
 (-> (.getBytes "13:Hello, World!," "UTF-8")
     ByteArrayInputStream.
     read-netstring
     (String. "UTF-8"))

 => Hello, World!
 ```

- `write-netstring`

 ```clojure
 (-> (doto (ByteArrayOutputStream.)
        (write-netstring (.getBytes "Hello, World!" "UTF-8")))
     .toString)

 => "13:Hello, World!,"
 ```

- `read-bencode`

 ```clojure
 (vec
   (map
     #(String. % "UTF-8")
     (-> (.getBytes "5:nrepl2:is7:awesomee" "UTF-8")
         ByteArrayInputStream.
         PushbackInputStream.
         read-bencode)))

=> ["nrepl" "is" "awesome"]
 ```

- `write-bencode`

 ```clojure
(-> (doto (ByteArrayOutputStream.)
      (write-bencode {:foo "bar"}))
    .toString)

=> "d3:foo3:bare"
 ```

Additionally, you can check this [document](https://github.com/nrepl/nrepl/blob/master/test/clojure/nrepl/bencode_test.clj) to learn more about it's usage.

## Use Cases

Obviously you can use this library whenever you need to deal with netstrings or
bencode, but I assume that in practice most people will end up using it for
building alternative Clojure nREPL clients or
servers. [babashka.nrepl](https://github.com/babashka/babashka.nrepl) is one
notable user of the library.

There's also the potential to have the library support ClojureScript and
ClojureCLR down the road, so it could be leveraged in even more
contexts. Sky is the limit!

## License

Copyright © 2018-2021 Meikel Brandmeyer, Bozhidar Batsov and nREPL contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
