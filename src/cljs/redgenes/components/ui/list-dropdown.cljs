(ns redgenes.components.ui.list-dropdown
  (:require [re-frame.core :refer [subscribe]]
            [reagent.core :as reagent]
            [imcljs.path :as im-path]
            [oops.core :refer [oget]]))

(defn has-text?
  "Return true if a label contains a string"
  [string details]
  (if string
    (re-find (re-pattern (str "(?i)" string)) (clojure.string/join " " (map details [:name :description])))
    true))

(defn has-type?
  "Return true if a label contains a string"
  [type list]
  (if type
    (= type (keyword (:type list)))
    true))

(defn im-lists []
  (fn [& {:keys [lists on-change]}]
    (into [:ul]
          (map (fn [{:keys [name title size]}]
                 [:li
                  {:on-click (partial on-change name)}
                  [:a [:span title] [:span.size (str " (" size ")")]]])) lists)))

(defn text-filter-form []
  (fn [text-filter-atom]
    [:form.form
     [:input.form-control
      {:type        "text"
       :value       @text-filter-atom
       :on-change   (fn [e] (reset! text-filter-atom (oget e :target :value)))
       :placeholder "Filter..."}]]))

(defn list-dropdown []
  "Creates a dropdown for intermine lists
  :value          The selected value to show in the dropdown
  :lists          A collection of Intermine lists
  :restrict-type  (Optional) a keyword to restrict the list to a type, like :Gene
  :on-change      A function to call with the name of the list"
  (let [text-filter-atom (reagent/atom nil)]
    (fn [& {:keys [value lists restrict-type on-change]}]
      (let [text-filter    (partial has-text? @text-filter-atom)
            type-filter    (partial has-type? restrict-type)
            filter-fn      (apply every-pred [text-filter type-filter])
            filtered-lists (filter filter-fn lists)]
        [:div.dropdown
         [:button.btn.btn-default.dropdown-toggle
          {:data-toggle "dropdown"}
          (str (or value "Choose a list") " ") [:span.caret]]
         [:div.dropdown-menu.dropdown-mixed-content
          [:div.container-fluid
           [text-filter-form text-filter-atom]
           [:div.col-md-6
            [:h4 [:i.fa.fa-clock-o] " Recently Created"]
            [im-lists
             :lists (take 5 (sort-by :timestamp filtered-lists))
             :on-change on-change]]
           [:div.col-md-6
            [:h4 [:i.fa.fa-sort-alpha-asc] " All Lists"]
            [:div.clip-400
             [im-lists
              :lists (sort-by :name filtered-lists)
              :on-change on-change]]]]]]))))