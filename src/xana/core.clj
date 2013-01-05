(ns xana.core
  (:use [xana.bibtex])
  (:use [xana.search]))

(defn setup
  []
  (clear-db)
  (load-db "/home/josh/git/misc/xana/test.bib")
  (new-index)
  (add-entries @db)
  (clear-search))
