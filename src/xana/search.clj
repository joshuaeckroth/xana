(ns xana.search
  (:require [clucy.core :as clucy]))

(def index (ref nil))

(def results (ref []))

(defn clear-search
  []
  (dosync (alter results (constantly []))))

(defn new-index
  []
  (dosync (alter index (constantly (clucy/memory-index)))))

(defn add-entries
  [entries]
  (apply clucy/add @index entries))

(defn search
  [query]
  (dosync (alter results (constantly (clucy/search @index query 100)))))
