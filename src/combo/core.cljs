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
        pub    (om/get-state owner :output-pub)
        in     (om/get-state owner :intern-chan)
        out    (om/get-state owner :commit-chan)]
    (async/sub pub :combo/commit in)
    (go-loop []
      (let [message (async/<! in)]
        (commit {:chan out :data data :owner owner :message message}))
      (recur))))

(defn- input! [owner]
  (partial async/put! (om/get-state owner :input-chan)))

(defn- local! [owner]
  (partial async/put! (om/get-state owner :local-chan)))

(defn- default-extern [input! owner]
  (when-let [c (om/get-state owner :extern-chan)]
    (go-loop []
      (input! (async/<! c))
      (recur))))

(defn- setup-extern [_ owner spec]
  (let [extern (:extern spec default-extern)]
    (extern (input! owner) owner)))

(defn- wrap-debug [spec behavior]
  (fn [state event]
    (when (:debug? spec)
      (println "<<" state " :: " event))
    (let [[new-state messages] (behavior state event)]
      (when (:debug? spec)
        (println "=>" new-state " :: " messages))
      [new-state messages])))

(defn- setup-behavior [data owner spec]
  (let [behavior (wrap-debug spec (:behavior spec (fn [s _] [s []])))
        input-chan (om/get-state owner :input-chan)
        output-chan (om/get-state owner :output-chan)]
    (go-loop [state {}]
      (let [[new-state messages] (behavior state (async/<! input-chan))]
        (doseq [m messages]
          (async/>! output-chan m))
        (recur new-state)))
    (async/put! input-chan [:combo/init :data data])))

(defn- unit-init-state [data spec]
  (let [props #(select-keys % [:value :options :class :disabled])]
    (merge
      (props spec)
      (when-let [v (get data (:id spec))]
        (if (map? v)
          (props v)
          {:value v})))))

(defn- unit-params [data owner spec layout]
  {:init-state (assoc (unit-init-state data spec)
                 :input-chan (om/get-state owner :input-chan)
                 :output-pub (om/get-state owner :output-pub))
   :opts (assoc spec :layout layout)})

(defn- build [data owner layout]
  (fn [spec]
    (om/build unit data
      (unit-params data owner (control layout spec) layout))))

(defn- nested [data owner spec]
  (let [nested (map (build data owner (:layout spec)) (:units spec))]
    (when-not (empty? nested)
      nested)))

(defn- unit [data owner spec]
  (reify
    
    om/IInitState
    (init-state [_]
      {:local-chan (async/chan)
       :output-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [validator   (:validator spec identity)
            local-chan  (om/get-state owner :local-chan)
            input-chan  (om/get-state owner :input-chan)
            output-chan (om/get-state owner :output-chan)
            output-pub  (om/get-state owner :output-pub)]
        (async/sub output-pub (:id spec) output-chan)
        (go-loop []
          (alt!
            local-chan
            ([v]
             (let [v (validator v)]
               (when-not (nil? v)
                 (om/set-state! owner :value v)
                 (async/>! input-chan [(:id spec) :value v]))
               (om/refresh! owner)))
            output-chan
            ([[_ key value]]
             (om/set-state! owner key value)))
          (recur))))

    om/IRenderState
    (render-state [_ state]
      (let [wrap (:wrap spec (fn [_ x] x))
            render (:render spec)]
        (wrap state
          (render (assoc state
                    :spec spec
                    :units (nested data owner spec)
                    :input! (input! owner)
                    :local! (local! owner))))))))

(defn view [data owner spec]
  (reify

    om/IInitState
    (init-state [_]
      (let [output-chan (async/chan)]
        {:output-chan output-chan
         :output-pub  (async/pub output-chan first)
         :input-chan  (async/chan)
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
