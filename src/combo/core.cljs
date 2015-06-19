(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(declare unit)

(defprotocol ILayout
  (render  [this build spec])
  (control [this spec]))

(def default-layout
  (reify ILayout
    (render [_ build spec]
      (apply dom/div nil (map build (:units spec))))
    (control [_ spec]
      spec)))

(defn- default-commit [{:keys [chan data owner message]}]
  (when chan
    (async/put! chan message))
  (when data
    (let [[_ attr value] message]
      (om/update! data attr value))))

(defn- setup-commit [data owner spec]
  (let [commit (:commit spec default-commit)
        pubc   (om/get-state owner :update-pubc)
        in     (om/get-state owner :intern-chan)
        out    (om/get-state owner :commit-chan)]
    (async/sub pubc :combo/commit in)
    (go-loop []
      (let [message (async/<! in)]
        (commit {:chan out
                 :data data
                 :owner owner
                 :message message}))
      (recur))))

(defn- default-extern [return owner]
  (when-let [c (om/get-state owner :extern-chan)]
    (go-loop []
      (return (async/<! c))
      (recur))))

(defn- setup-extern [_ owner spec]
  (let [extern (:extern spec default-extern)
        return (partial async/put! (om/get-state owner :return-chan))]
    (extern return owner)))

(defn- wrap-debug [spec behavior]
  (fn [message state]
    (when (:debug? spec) (println "<<" message " :: " state))
    (let [[messages new-state] (behavior message state)]
      (when (:debug? spec) (println "=>" messages " :: " new-state))
      [messages new-state])))

(defn- debug-behavior [messages state spec]
  (when (:debug? spec)
    (println "Behavior:" messages "with state:" state)))

(defn- setup-behavior [data owner spec]
  (let [behavior (wrap-debug spec (:behavior spec (fn [_ s] [[] s])))
        return-chan (om/get-state owner :return-chan)
        update-chan (om/get-state owner :update-chan)]
    (go-loop [state {}]
      (let [[messages new-state] (behavior (async/<! return-chan) state)]
        (doseq [m messages]
          (async/>! update-chan m))
        (recur new-state)))
    (async/put! return-chan [:combo/init :data data])))

(defn- unit-init-state [data spec]
  (let [props #(select-keys % [:value :options :class :disabled])]
    (merge
      (props spec)
      (when-let [v (get data (:entity spec))]
        (if (map? v)
          (props v)
          {:value v})))))

(defn- unit-params [data owner spec layout]
  {:init-state (assoc (unit-init-state data spec)
                 :update-pubc (om/get-state owner :update-pubc)
                 :return-chan (om/get-state owner :return-chan))
   :opts (assoc spec :layout layout)})

(defn- build [data owner layout]
  (fn [spec]
    (om/build unit data
      (unit-params data owner (control layout spec) layout))))

(defn- nested [data owner spec]
  (when-let [units (:units spec)]
    (map (build data owner (:layout spec)) units)))

(defn- render-unit [owner spec units]
  (let [f (:render spec)]
    (if units
      (f owner spec units)
      (f owner spec))))

(defn- unit [data owner spec]
  (reify
    
    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)
       :update-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [interceptor (:interceptor spec identity)
            change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)
            update-pubc (om/get-state owner :update-pubc)]
        (async/sub update-pubc (:entity spec) update-chan)
        (go-loop []
          (alt!
            change-chan
            ([v]
             (let [v (interceptor v)]
               (when-not (nil? v)
                 (om/set-state! owner :value v)
                 (async/>! return-chan [(:entity spec) :value v]))
               (om/refresh! owner)))
            update-chan
            ([[_ attr value]]
             (om/set-state! owner attr value)))
          (recur))))

    om/IRender
    (render [_]
      (let [wrap (:wrap spec (fn [_ x] x))
            units (nested data owner spec)
            content (render-unit owner spec units)]
        (wrap owner content)))))

(defn view [data owner spec]
  (reify

    om/IInitState
    (init-state [_]
      (let [update-chan (async/chan)]
        {:update-chan update-chan
         :update-pubc (async/pub update-chan first)
         :return-chan (async/chan)
         :intern-chan (async/chan)}))

    om/IWillMount
    (will-mount [_]
      (setup-commit   data owner spec)
      (setup-extern   data owner spec)
      (setup-behavior data owner spec))

    om/IRender
    (render [_]
      (let [layout (:layout spec default-layout)]
        (render layout (build data owner layout) spec)))))
