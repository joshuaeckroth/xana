(ns xana.gui
  (:import (javax.swing JFrame))
  (:use [seesaw.core])
  (:use [seesaw.mig])
  (:use [seesaw.table])
  (:use [seesaw.swingx])
  (:use [xana.core])
  (:use [xana.bibtex])
  (:use [xana.search]))

(declare mainwindow)

(def results-table-model
  (table-model :columns
               [{:key :BibTeXkey :text "BibTeX key"}
                {:key :author :text "Author"}
                {:key :title :text "Title"}
                {:key :year :text "Year"}]))

(defn clear-results-table
  []
  (clear! results-table-model)
  (.clearSelection (select mainwindow [:#results-table])))

(defn populate-table
  [results]
  (clear-results-table)
  (doseq [i (range (count results))]
    (insert-at! results-table-model i (nth results i))))

(def summary-textbox
  (editor-pane :content-type "text/html"))

(defn search-listener
  [e]
  (when (= \newline (.getKeyChar e))
    (let [search-input (select mainwindow [:#search-input])
          query (value search-input)]
      (search query)
      (populate-table (sort-by :BibTeXkey (vals @results))))))

(defn result-selection-listener
  [e]
  (try
    (let [t (select mainwindow [:#results-table])
          bibtex-key (.getValueAt t (.getSelectedRow t) 0)]
      (value! summary-textbox
              (if-let [frags (:_fragments (meta (get @results bibtex-key)))]
                (format "%s<br/><br/>%s" (format-entry bibtex-key) frags)
                (format-entry bibtex-key)))
      (when (= 2 (.getClickCount e))
        (open-file bibtex-key)))
    (catch Exception e)))

(def mainwindow
  (frame :title "xana"
         :content
         (mig-panel
          :constraints ["fill"]
          :items [[(label "Search") "grow 0"]
                  [(text :id :search-input :columns 30
                         :listen [:key-pressed search-listener])
                   "wrap, growy 0, growx 1"]
                  [(scrollable (table-x :id :results-table :model results-table-model
                                        :listen [:selection result-selection-listener
                                                 :mouse-clicked result-selection-listener]))
                   "w 900px, h 300px, span 2, grow, wrap"]
                  [(scrollable summary-textbox) "h 300, span2, grow"]])))

(defn replwindow
  []
  (setup)
  (clear-results-table)
  (invoke-later
   (-> mainwindow
      pack!
      show!)))

(defn -main
  [& args]
  (setup)
  (invoke-later
   (-> (doto mainwindow (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))
      pack!
      show!)))
