;*************************************************************
;*************************************************************
;*****     REMEMBER TO ENTER EACH NAMESPACE TWICE!!!     *****
;*************************************************************
;*************************************************************

(ns tst.flintstones.doorunner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [schema.core :as s]

    [tst.flintstones.dino]
    [tst.flintstones.wilma]
    [tst.flintstones.pebbles]
    [tst.flintstones.slate]
    [tst.flintstones.bambam]

    [tst.tupelo._bootstrap]
    [tst.tupelo.core]
  ))

; (s/set-fn-validation! true) ; enforce fn schemas

(enable-console-print!)
(println "doorunner - beginning")

(doo-tests
  'tst.flintstones.dino
  'tst.flintstones.wilma
  'tst.flintstones.pebbles
  'tst.flintstones.slate
  'tst.flintstones.bambam

  'tst.tupelo._bootstrap
  'tst.tupelo.core
)
(println "doorunner - end")
