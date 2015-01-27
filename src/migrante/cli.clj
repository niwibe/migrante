(ns migrations.cli)
;; (:require [clojure.java.jdbc :as jdbc])
;; (:require [migrations.core :as core])
;; (:require [clojure.pprint :refer [pprint]])
;; (:gen-class))

;; (defn- ordered-migrations
;;   "Analyze all migrations and return dependency ordered
;;   vector with migration functions."
;;   [migration-modules]
;;   (let [result (atom [])]
;;     (doseq [[nsname migrations] (seq migration-modules)]
;;       (loop [parent nil
;;              migs migrations]
;;         (if (seq migs)
;;           (do
;;             (let [m1 (filter (fn [x]
;;                                (let [metadata (meta @x)]
;;                                  (= (:migration-parent metadata) parent))) migs)
;;                   m1 (first m1)
;;                   m2 (filter (fn [x]
;;                                (let [metadata (meta @x)]
;;                                  (not= (:migration-parent metadata) parent))) migs)]
;;               (if (nil? m1) nil
;;                 (do
;;                   (swap! result conj m1)
;;                   (recur (:migration-name (meta @m1)) m2))))))))
;;     @result))

;; (defn- cmdlist-print-migrationstatus
;;   [fnvar]
;;   (let [metadata        (meta @fnvar)
;;         module-name     (:migration-module metadata)
;;         migration-name  (:migration-name metadata)]
;;     (if (core/applied-migration? module-name migration-name)
;;       (println (format "[x] - %s/%s" module-name migration-name))
;;       (println (format "[ ] - %s/%s" module-name migration-name)))))

;; (defn- cli-command-list
;;   [project]
;;   (doseq [m (ordered-migrations @core/*migrations*)]
;;     (cmdlist-print-migrationstatus m)))

;; (defn- cli-command-migrate-all
;;   [project]
;;   (doseq [m (ordered-migrations @core/*migrations*)]
;;     (let [metadata        (meta @m)
;;           module-name     (:migration-module metadata)
;;           migration-name  (:migration-name metadata)]
;;       (when-not (core/applied-migration? module-name migration-name)
;;         (apply (:up @m) [core/*db*])
;;         (core/apply-migration module-name migration-name)
;;         (println (format "Applying migration: %s/%s" module-name migration-name))))))

;; (defn- cli-command-rollback
;;   [project module-name migration-name]
;;   (let [filtered          ((keyword module-name) @core/*migrations*)
;;         rollback-pending  (atom [])]
;;     (loop [ms (reverse filtered)]
;;       (let [metadata  (meta @(first ms))
;;             modname   (:migration-module metadata)
;;             migname   (:migration-name metadata)]

;;         (swap! rollback-pending conj (first ms))
;;         (when-not (= migname migration-name)
;;           (recur (next ms)))))

;;     (doseq [m @rollback-pending]
;;       (let [metadata        (meta @m)
;;             module-name     (:migration-module metadata)
;;             migration-name  (:migration-name metadata)]
;;         (when (core/applied-migration? module-name migration-name)
;;           (apply (:down @m) [core/*db*])
;;           (core/rollback-migration module-name migration-name)
;;           (println (format "Rollback migration: %s/%s" module-name migration-name)))))))

;; (defn cli-command-help
;;   "Simply manage sql migrations with clojure/jdbc

;; Commands:
;;   list                                  List all migrations and their status.
;;   migrate-all                           Run all pending migrations.
;;   rollback [modulename] [migration]     Rollback to specified migration including it.
;;   help                                  Show this help."
;;   [project & args]
;;   (println (:doc (meta #'cli-command-help))))

;; (defn- cli-command-default
;;   [project command]
;;   (cli-command-help project))

;; (defn run-cli
;;   [project dbspec command & args]
;;   (do
;;     (core/bootstrap dbspec)
;;     (core/load-migration-modules (:migrations project))
;;     (jdbc/db-transaction [db dbspec]
;;       (binding [core/*db* db]
;;         (cond
;;           (= command "list") (cli-command-list project)
;;           (= command "migrate-all") (cli-command-migrate-all project)
;;           (= command "rollback") (apply cli-command-rollback (cons project (vec args)))
;;           (= command "help") (cli-command-help project)
;;           :else (cli-command-default project command))))))