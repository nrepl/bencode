;; Copyright (c) Meikel Brandmeyer. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.


(ns bencode.core-test
  (:require [clojure.test :refer [are deftest is]]
            [bencode.core :as bencode :refer [read-bencode
                                              read-netstring
                                              write-bencode
                                              write-netstring]])
  (:import clojure.lang.RT
           #?(:clj
              [java.io ByteArrayInputStream ByteArrayOutputStream PushbackInputStream]
              :cljr
              [System.IO MemoryStream Stream])))

(defn #^{:private true} >bytes
  [#^String input]
  #?(:clj (.getBytes input "UTF-8")
     :cljr (.GetBytes System.Text.Encoding/UTF8 input)))

(defmulti #^{:private true} <bytes class)

(defmethod <bytes :default
  [input]
  input)

#?(:clj
   (defmethod <bytes (RT/classForName "[B")
     [#^"[B" input]
     (String. input "UTF-8"))
   :cljr
   (defmethod <bytes (RT/classForName "System.Byte[]")
     [#^"System.Byte[]" input]
     (.GetString System.Text.Encoding/UTF8 input)))

(defmethod <bytes clojure.lang.IPersistentVector
  [input]
  (vec (map <bytes input)))

(defmethod <bytes clojure.lang.IPersistentMap
  [input]
  (->> input
       (map (fn [[k v]] [k (<bytes v)]))
       (into {})))

#?(:clj
   (defn- decode
     [bytes & {:keys [reader]}]
     (-> bytes
         ByteArrayInputStream.
         PushbackInputStream.
         reader))
   :cljr
   (defn- decode
     [bytes & {:keys [reader]}]
     (let [stream (MemoryStream. bytes)]
       (try (reader stream)
         (finally
           (.Dispose stream))))))

#?(:clj
   (defn- >input
     [^String input & args]
     (-> input
         (.getBytes "UTF-8")
         (#(apply decode % args))
         <bytes))
   :cljr
   (defn- >input
     [^String input & args]
     (-> (.GetBytes System.Text.Encoding/UTF8 input)
         (#(apply decode % args))
         <bytes)))

(deftest test-netstring-reading
  (are [x y] (= (>input x :reader read-netstring) y)
    "0:,"                ""
    "13:Hello, World!,"  "Hello, World!"
    "16:Hällö, Würld!,"  "Hällö, Würld!"
    "25:Здравей, Свят!," "Здравей, Свят!"))

(deftest test-string-reading
  (are [x y] (= (>input x :reader read-bencode) y)
    "0:"                ""
    "13:Hello, World!"  "Hello, World!"
    "16:Hällö, Würld!"  "Hällö, Würld!"
    "25:Здравей, Свят!" "Здравей, Свят!"))

(deftest test-integer-reading
  (are [x y] (= (>input x :reader read-bencode) y)
    "i0e"     0
    "i42e"   42
    "i-42e" -42))

(deftest test-list-reading
  (are [x y] (= (>input x :reader read-bencode) y)
    "le"                    []
    "l6:cheesee"            ["cheese"]
    "l6:cheese3:ham4:eggse" ["cheese" "ham" "eggs"]))

(deftest test-map-reading
  (are [x y] (= (>input x :reader read-bencode) y)
    "de"            {}
    "d3:ham4:eggse" {"ham" "eggs"}))

(deftest test-nested-reading
  (are [x y] (= (>input x :reader read-bencode) y)
    "l6:cheesei42ed3:ham4:eggsee" ["cheese" 42 {"ham" "eggs"}]
    "d6:cheesei42e3:haml4:eggsee" {"cheese" 42 "ham" ["eggs"]}))

#?(:clj
   (defn- >stream
     [thing & {:keys [writer]}]
     (doto (ByteArrayOutputStream.)
           (writer thing)))
   :cljr
   (defn- >stream
     [thing & {:keys [writer]}]
     (let [ms (MemoryStream.)]
       (try
         (doto ms (writer thing))
         (finally
           (.Dispose ms))))))

(defn- >output
  [& args]
  #?(:clj (.toString (apply >stream args) "UTF-8")
     :cljr (.GetString System.Text.Encoding/UTF8 (.ToArray (apply >stream args)))))

(deftest test-netstring-writing
  (are [x y] (= (>output (>bytes x) :writer write-netstring) y)
    ""               "0:,"
    "Hello, World!"  "13:Hello, World!,"
    "Hällö, Würld!"  "16:Hällö, Würld!,"
    "Здравей, Свят!" "25:Здравей, Свят!,"))

(deftest test-byte-array-writing
  (are [x y] (= (>output (>bytes x) :writer write-bencode) y)
    ""               "0:"
    "Hello, World!"  "13:Hello, World!"
    "Hällö, Würld!"  "16:Hällö, Würld!"
    "Здравей, Свят!" "25:Здравей, Свят!"))

(deftest test-string-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    ""               "0:"
    "Hello, World!"  "13:Hello, World!"
    "Hällö, Würld!"  "16:Hällö, Würld!"
    "Здравей, Свят!" "25:Здравей, Свят!"))

#?(:clj
   (deftest test-input-stream-writing
     (are [x y] (= (>output (ByteArrayInputStream. (>bytes x))
                            :writer write-bencode) y)
          ""               "0:"
          "Hello, World!"  "13:Hello, World!"
          "Hällö, Würld!"  "16:Hällö, Würld!"
          "Здравей, Свят!" "25:Здравей, Свят!"))
   :cljr
   (deftest test-input-stream-writing
     (are [x y]
          (let [ms (MemoryStream. (>bytes x))]
            (try
              (= (>output ms :writer write-bencode) y)
              (finally (.Dispose ms))))
          ""               "0:"
          "Hello, World!"  "13:Hello, World!"
          "Hällö, Würld!"  "16:Hällö, Würld!"
          "Здравей, Свят!" "25:Здравей, Свят!")))

(deftest test-integer-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    0 "i0e"
    42 "i42e"
    -42 "i-42e"

    ;; Works for all integral types.
    ;; Note: BigInts (42N) not tested, since they are not
    ;; supported in 1.2.
    (byte "42")    "i42e"
    (short "42")   "i42e"
    (int "42") "i42e"
    (long "42")    "i42e"))

(deftest test-named-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    :foo      "3:foo"
    :foo/bar  "7:foo/bar"
    'foo      "3:foo"
    'foo/bar  "7:foo/bar"))


(deftest test-list-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    nil                     "le"
    []                      "le"
    ["cheese"]              "l6:cheesee"
    ["cheese" "ham" "eggs"] "l6:cheese3:ham4:eggse"))

(deftest test-map-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    {}             "de"
    {"ham" "eggs"} "d3:ham4:eggse"))

(deftest test-nested-writing
  (are [x y] (= (>output x :writer write-bencode) y)
    ["cheese" 42 {"ham" "eggs"}] "l6:cheesei42ed3:ham4:eggsee"
    {"cheese" 42 "ham" ["eggs"]} "d6:cheesei42e3:haml4:eggsee"))

(deftest test-lexicographic-sorting
  (let [source   ["ham" "eggs" "hamburg" "hamburger" "cheese"]
        expected ["cheese" "eggs" "ham" "hamburg" "hamburger"]
        to-test  (->> source
                      (map >bytes)
                      (sort @#'bencode.core/lexicographically)
                      (map <bytes))]
    (is (= to-test expected))))

(deftest unencoded-values
  ;; just some PNG data that won't round-trip cleanly through UTF-8 encoding, so
  ;; any default encoding in the bencode implementation will be caught immediately
  (let [binary-data (->> [-119 80 78 71 13 10 26 10 0 0 0 13 73 72 68 82 0 0 0
                          100 0 0 0 100 8 6 0 0 0 112 -30 -107 84 0 0 3 -16 105
                          67 67 80 73 67 67 32 80 114 111 102 105 108 101 0 0 40
                          -111 -115 85 -35 111 -37 84 20 63 -119 111 92 -92 22 63
                          -96 -79 -114 14 21 -117 -81 85 83 91 -71 27 26 -83 -58 6
                          73 -109 -91 -23 66 26 -71 -51 -40 42 -92 -55 117 110]
                         #?(:cljr (map #(+ 127 %)))
                         (map byte)
                         (into-array #?(:clj Byte/TYPE :cljr System.Byte)))]
    (is (= (seq binary-data)
           (-> {"data" binary-data}
               (>stream :writer write-bencode)
               #?(:clj .toByteArray :cljr .ToArray)
               (decode :reader read-bencode)
               (get "data")
               seq)))))
