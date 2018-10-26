[![Build Status](https://travis-ci.org/nrepl/bencode.svg?branch=master)](https://travis-ci.org/nrepl/bencode)
[![Clojars Project](https://img.shields.io/clojars/v/nrepl/bencode.svg)](https://clojars.org/nrepl/bencode)
[![cljdoc badge](https://cljdoc.org/badge/nrepl/bencode)](https://cljdoc.org/d/nrepl/bencode/CURRENT)

# bencode

A netstring and bencode implementation for Clojure.

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

This particular implementation was extracted from nREPL, so it could be reused by other
applications and be made ClojureScript-compatible as well.

There are other netstring/bencode libraries for Clojure out there, but this one has
the distinct advantage that it's certainly going to work well with nREPL. ;-)

## Usage

Just add `bencode` as a dependency to your project and start hacking.

```clojure
[nrepl/bencode "1.0.0"]
```

The API is documented in great detail [here](https://github.com/nrepl/bencode/blob/master/src/bencode/core.clj).

The main functions in the API are:

* `read-netsring`
* `write-nestring`
* `read-bencode`
* `write-bencode`

## License

Copyright © 2018 Meikel Brandmeyer, Bozhidar Batsov and nREPL contributors

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
