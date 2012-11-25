(ns xana.bibtex
  (:import (java.io FileReader StringWriter))
  (:import (org.jbibtex BibTeXDatabase BibTeXFormatter BibTeXParser)))

(defn load-bibtex
  [file]
  (let [parser (BibTeXParser.)]
    (.parse parser (FileReader. file))))

(defn format-bibtex
  [db]
  (let [formatter (BibTeXFormatter.)
        s (StringWriter.)]
    (.format formatter db s)
    s))

(defn bibtex-entries
  [db]
  (apply merge
         (for [e (.getEntries db)]
           {(.getValue (.getKey e))
            (apply hash-map
                   (mapcat (fn [kv] [(keyword (.getValue (.getKey kv)))
                                    (.getString (.getValue kv))])
                           (.entrySet (.getFields (.getValue e)))))})))
