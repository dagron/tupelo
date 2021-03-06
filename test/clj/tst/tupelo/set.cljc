;   Copyright (c) Alan Thompson. All rights reserved.
;   The use and distribution terms for this software are covered by the Eclipse Public License 1.0
;   (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at
;   the root of this distribution.  By using this software in any fashion, you are agreeing to be
;   bound by the terms of this license.  You must not remove this notice, or any other, from this
;   software.
(ns tst.tupelo.set
  #?@(:clj [
  (:use tupelo.test )
  (:require
    [tupelo.set :as set]
    [clojure.set :as raw]) ]))

#?(:clj
   (do
     (dotest
       (is= #{1 2 3 4 5} (set/union #{1 2 3 4} #{2 3 4 5}))
       (throws? (set/union #{1 2 3 4} {2 3 4 5}))
       (throws? (set/union #{1 2 3 4} [2 3 4 5]))
     )

   ; #todo tests for clojure.set stuff

))
