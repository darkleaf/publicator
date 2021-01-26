(ns publicator.core.use-cases.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]))

(defn new-validators [user]
  (-> (agg/new-validators)
      (agg/upsert-predicate-validator :user/login #"\w{3,255}")
      (agg/upsert-required-validator  :user/login agg/root-entity-rule)

      (agg/upsert-predicate-validator :user/state [:active :archived])
      (agg/upsert-required-validator  :user/state agg/root-entity-rule)

      (agg/upsert-predicate-validator :user/password-digest #".{1,255}")
      (agg/upsert-required-validator  :user/password-digest agg/root-entity-rule)

      ;; admin? и author? сделаны отдельными полями
      ;; для разграничения доступа по полям
      (agg/upsert-predicate-validator :user/admin? boolean?)
      (agg/upsert-predicate-validator :user/author? boolean?)

      (cond-> (author? user) (author/upsert-validators))))

(defn author? [user]
  (-> user agg/root (get :user/author? false)))
