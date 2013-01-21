(ns ^{:doc "Facts as something you check"}
  midje.checking.facts
  (:require [midje.config :as config]
            [midje.emission.boundaries :as emission-boundary]
            [midje.internal-ideas.fact-context :as fact-context]
            [midje.internal-ideas.compendium :as compendium]))
                  
;;; Fact execution utilities

(defn check-one [fact]
  (let [top-level? (:midje/top-level-fact? (meta fact))
        fact-creation-filter (config/choice :desired-fact?)]
    (cond (not (fact-creation-filter fact))
          (str "This fact was ignored because of the current configuration. "
               "Only facts matching "
               (vec (map #(if (fn? %) "<some function>" %) 
                         (:created-from (meta fact-creation-filter))))
               " will be created.")
            
          (not top-level?)
          (emission-boundary/around-fact fact
            (fact))

          :else-top-level-fact-to-check
          (emission-boundary/around-top-level-fact fact {}
            ;; The fact is recorded on entry so that if a fact is
            ;; rechecked inside the midje.t-repl tests, the rechecked
            ;; fact stays the last-fact-checked (because it overwrites
            ;; the fact that's testing it).
            (compendium/record-fact-check! fact)
            (emission-boundary/around-fact fact
              (fact))))))

(defn creation-time-check [fact]
  (when (config/choice :check-after-creation)
    (check-one fact)))
