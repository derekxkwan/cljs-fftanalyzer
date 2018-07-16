
(ns cljs-fftanalyzer.core
    (:require [cljs-fftanalyzer.fft :as fft]
              [reagent.core :as r]
              ))


(def fft-size (r/atom 4096))
(def win js/window)
(def ctx nil)
(def cnv-prop {:w 500 :h 300})
(def app-state (r/atom {:sr nil :enabled false}))

;; since there's no function hoisting in clojurescript...
(defn enable-audio []
  (set! ctx (let [a-ctx (win.AudioContext.)
                  wk-ctx (win.webkitAudioContext.)]
              (or (a-ctx) (wk-ctx))))
  
  (fft/set-canvas (.getElementById js/document "cnv"))
  (fft/create-analyzer ctx @fft-size)
  (fft/set-mic-input ctx)
  (fft/draw)
  )

; view stuff


(defn provide-canvas []
  [:canvas {:id "cnv" :width (:w cnv-prop) :height (:h cnv-prop)}]
)


(defn provide-dropdown []
  [:label 
  [:select {:on-change #(let [tval (-> % .-target .-value)]
                          (.log js/console tval)
                          (reset! fft-size tval))
            :default-value @fft-size
            }
                            
   (doall (for [i (range 5 14)
                :let [i-map (Math/pow 2 i)
                      params {:key i-map :value i-map}
                      ]]
     [:option params (str i-map)]
     ))]
   " select fft-size (only works before enabling audio)"]
  )

(defn show-sr []
  (let [sr (if (not (nil? ctx)) (.-sampleRate ctx) "not loaded")]
    (swap! app-state merge {:sr sr})
    (str " current sample rate: " (str (get-in @app-state [:sr])))
    ))


(defn provide-button []
  [:input {:type "button"
           :value "enable mic"
           :on-click #(do (swap! app-state merge @app-state {:enabled true})
                           (enable-audio))
           }])
                           


(defn page []
  [:div
   [:div
    (provide-button)[:span (show-sr)]
    [:br](provide-dropdown)
    ]
   [:div
    (provide-canvas)
    ]
   ]
  
  
)

(defn render []
(r/render-component [page] (.getElementById js/document "app")))

(render)
;;sound stuff
