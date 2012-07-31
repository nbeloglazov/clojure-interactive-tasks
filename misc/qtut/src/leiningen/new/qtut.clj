(ns leiningen.new.qtut
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer "qtut"))

(defn qtut
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             ["src/{{sanitized}}/core.clj" (render "core.clj" data)]
             ["src/{{sanitized}}/work.clj" (render "work.clj" data)]
             ["project.clj" (render "project.clj" data)])))
