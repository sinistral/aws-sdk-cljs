
(ns sinistral.aws-sdk-cljs.core
  #?(:cljs (:require-macros [sinistral.aws-sdk-cljs.core :refer [<? async-try]]))
  (:require [clojure.core.async :as async]))

(defmacro async-try
  [& body]
  `(async/go
     (try
       ~@body
       (catch :default e
         e))))

(defn ^{:private true} throw-err
  [e]
  (when (instance? #?(:clj Exception :cljs js/Error) e)
    (throw e))
  e)

(defmacro <?
  [ch]
  `(throw-err (async/<! ~ch)))

#?(:cljs (defn async-aws-call
           [f]
           (let [chan (async/chan 1)]
             (-> (f)
                 (.on "success"
                      #(async/go
                         (let [val {:data (js->clj (.-data %) :keywordize-keys true)}]
                           (async/>! chan val))))
                 (.on "error"
                      #(async/go
                         (let [val {:error {:code (.-code %) :message (.-message %)}}]
                           (async/>! chan val))))
                 (.send))
             chan)))
