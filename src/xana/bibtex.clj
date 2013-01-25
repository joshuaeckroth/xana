(ns xana.bibtex
  (:import (java.io FileReader StringWriter))
  (:import (org.jbibtex BibTeXDatabase BibTeXFormatter BibTeXParser))
  (:import (org.jbibtex.citation ACSReferenceStyle ReferenceFormatter))
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.string :as str]))

(def db (ref []))

(defn format-entry
  [bibtex-key]
  (let [formatter (ReferenceFormatter. (ACSReferenceStyle.))
        entry (first (filter #(= bibtex-key (:BibTeXkey %)) @db))]
    (try
      (format "%s\n\n%s"
         (.format formatter (:jbibtex-entry entry) false)
         (if-let [abstract (:abstract entry)]
           (str/replace (str/replace abstract "\n" " ") #"^\s+" "")
           ""))
      (catch Exception e (format "Error formatting: %s" (str e))))))

(defn open-file
  [bibtex-key]
  (let [entry (first (filter #(= bibtex-key (:BibTeXkey %)) @db))]
    (when-let [file (:file entry)]
      (sh "xdg-open" (format "/home/josh/research/papers/%s" file)))))

(defn set-file-location
  [file]
  (when file
    (str/replace file #":.*" "")))

(defn bibtex-entries
  [jbibtex-db]
  (for [e (.getEntries jbibtex-db)]
    (update-in
     (merge (apply hash-map
                   (mapcat (fn [kv] [(keyword (.getValue (.getKey kv)))
                                    (.getString (.getValue kv))])
                           (.entrySet (.getFields (.getValue e)))))
            {:BibTeXkey (.getValue (.getKey e))
             :jbibtex-entry (.getValue e)})
     [:file] set-file-location)))

(defn output-bibtex
  [jbibtex-db]
  (let [formatter (BibTeXFormatter.)
        s (StringWriter.)]
    (.format formatter jbibtex-db s)
    s))

(defn load-bibtex
  [file]
  (let [parser (BibTeXParser.)]
    (.parse parser (FileReader. file))))

(defn clear-db
  []
  (dosync (alter db (constantly []))))

(defn load-db
  [filename]
  (dosync (alter db (constantly (bibtex-entries (load-bibtex filename))))))
