(ns tst.tupelo.java-time
  (:refer-clojure :exclude [range])
  (:use tupelo.java-time tupelo.core tupelo.test)
  (:require
    [clj-time.core :as joda]
    [clojure.string :as str])
  (:import (java.time ZonedDateTime ZoneId Duration)
           [java.time.temporal ChronoUnit TemporalAdjuster TemporalAdjusters]))

(dotest
  (is (temporal? (ZonedDateTime/parse "2018-09-08T13:03:04.500Z")))
  (is (temporal? (ZonedDateTime/parse "2018-09-08T13:03:04Z")))
  (is (temporal? (ZonedDateTime/parse "2018-09-08T00:00Z")))

  (is (fixed-time-point? (zoned-date-time 2018 9 1)))
  (is (fixed-time-point? (->instant (zoned-date-time 2018 9 1))))
  (is (fixed-time-point? (joda/date-time 2018 9 1)))

  (is= {:zdt     "2018-09-01T00:00:00Z",
        :instant "2018-09-01T00:00:00Z",
        :joda-dt "2018-09-01T00:00:00Z"}
    (stringify-times
      {:zdt     (zoned-date-time 2018 9 1)
       :instant (->instant (zoned-date-time 2018 9 1))
       :joda-dt (->instant (zoned-date-time 2018 9 1))}) )

  ;(is (period? (time/days 3)))
  ;(is (period? (time/weeks 3)))
  ;(is (period? (time/months 3)))
  ;(is (period? (time/years 3)))

  )


(dotest
  (let [zone-ids (vec (sort (ZoneId/getAvailableZoneIds))) ; all ZoneId String values
        zone-ids-america (vec (keep-if #(str/starts-with? % "America/" ) zone-ids))
        zone-ids-europe (vec (keep-if #(str/starts-with? % "Europe/" ) zone-ids))
        zone-ids-us (vec (keep-if #(str/starts-with? % "US/" ) zone-ids)) ]
    (is (< 590 (count zone-ids)))
    (is (< 160 (count zone-ids-america)))
    (is (<  60 (count zone-ids-europe)))
    (is (<  10 (count zone-ids-us))))

  (let [ref           (ZonedDateTime/of 2018 2 3 4 5 6 123456789 (ZoneId/of "UTC"))
        ref-from-str  (ZonedDateTime/parse "2018-02-03T04:05:06.123456789Z") ; zulu = utc
        inst          (.toInstant ref)
        inst-from-str (.toInstant ref-from-str)]
    ; time zone info is presereved in ZonedDateTime, so they are not "equal" objects
    (isnt (.equals ref ; #object[java.time.ZonedDateTime 0x4b881b83 "2018-02-03T04:05:06.123456789Z[UTC]"]
            ref-from-str)) ; #object[java.time.ZonedDateTime 0x6ada3244 "2018-02-03T04:05:06.123456789Z"]
    (isnt= ref ref-from-str)
    (isnt= 0 (.compareTo ref ref-from-str))

    ; they are identical instants
    (is= inst inst-from-str)
    (is (.isEqual ref ref-from-str)) ; converts to Instant, then compares
    (is (same-instant? ref ref-from-str)) ; more Clojurey way

    (is (same-instant? (zoned-date-time 2018)                 (trunc-to-year ref)))
    (is (same-instant? (zoned-date-time 2018 2)               (trunc-to-month ref)))
    (is (same-instant? (zoned-date-time 2018 2 3)             (trunc-to-day ref)))
    (is (same-instant? (zoned-date-time 2018 2 3 ,, 4)        (trunc-to-hour ref)))
    (is (same-instant? (zoned-date-time 2018 2 3 ,, 4 5)      (trunc-to-minute ref)))
    (is (same-instant? (zoned-date-time 2018 2 3 ,, 4 5 6)    (trunc-to-second ref)))
    (is (same-instant? (zoned-date-time 2018 2 3 ,, 4 5 6 ,, 123456789) ref))
    (is (same-instant? (zoned-date-time 2018 2 3 ,, 4 5 6 ,, 123456789 zoneid-utc) ref))

    (is (same-instant? ref (with-zoneid zoneid-utc
                             (zoned-date-time 2018 2 3 ,, 4 5 6 123456789))))
    (is (same-instant?                    (zoned-date-time 2018 2 3 ,, 12 5 6 ,, 123456789)
          (with-zoneid zoneid-us-eastern  (zoned-date-time 2018 2 3 ,,  7 5 6 ,, 123456789))
          (with-zoneid zoneid-us-central  (zoned-date-time 2018 2 3 ,,  6 5 6 ,, 123456789))
          (with-zoneid zoneid-us-mountain (zoned-date-time 2018 2 3 ,,  5 5 6 ,, 123456789))
          (with-zoneid zoneid-us-pacific  (zoned-date-time 2018 2 3 ,,  4 5 6 ,, 123456789)))))

  (is (same-instant? (zoned-date-time 2018 8 26)
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 1))))
  (is (same-instant? (zoned-date-time 2018 9 2)
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 2))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 3))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 4))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 5))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 6))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 7))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 8))))
  (is (same-instant? (zoned-date-time 2018 9 9)
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 9))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 10))
        (trunc-to-sunday-midnight (zoned-date-time 2018 9 10 2 3 4))))
  (let [zdt (zoned-date-time 2018 10 7)]
    (is (same-instant? (zoned-date-time 2018 10 1) (trunc-to-monday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 2) (trunc-to-tuesday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 3) (trunc-to-wednesday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 4) (trunc-to-thursday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 5) (trunc-to-friday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 6) (trunc-to-saturday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 10 7) (trunc-to-sunday-midnight zdt))))
  (let [zdt (zoned-date-time 2018 9 7)]
    (is (same-instant? (zoned-date-time 2018 9 1) (trunc-to-saturday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 2) (trunc-to-sunday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 3) (trunc-to-monday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 4) (trunc-to-tuesday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 5) (trunc-to-wednesday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 6) (trunc-to-thursday-midnight zdt)))
    (is (same-instant? (zoned-date-time 2018 9 7) (trunc-to-friday-midnight zdt))) ) )

(dotest
  (let [zdt (zoned-date-time 2018 9 8,, 2 3 4)]
    (is= (string-date-iso zdt)            "2018-09-08")
    (is= (string-date-time-iso zdt)       "2018-09-08T02:03:04Z")
    (is= (string-date-time-nice zdt)      "2018-09-08 02:03:04Z")
    (is= (iso-date-str zdt)            "2018-09-08") ; deprecated
    (is= (iso-date-time-str zdt) "2018-09-08T02:03:04Z")) ; deprecated
  (let [zdt (zoned-date-time 2018 9 8,, 2 3 4,, 123456789)]
    (is= (string-date-compact zdt)        "20180908" )
    (is= (string-date-time-nice zdt)      "2018-09-08 02:03:04.123456789Z")
    (is= (string-date-time-compact zdt)   "20180908-020304" )
    (is= (string-date-time-hyphens zdt)   "2018-09-08-02-03-04")
    (is= (iso-date-str zdt)            "2018-09-08") ; deprecated
    (is= (iso-date-time-str zdt)       "2018-09-08T02:03:04.123456789Z") ; deprecated
  ))

(dotest
  (is= [(zoned-date-time 2018 9 1)
        (zoned-date-time 2018 9 2)
        (zoned-date-time 2018 9 3)
        (zoned-date-time 2018 9 4)]
       (range
         (zoned-date-time 2018 9 1)
         (zoned-date-time 2018 9 5)
         (Duration/ofDays 1)))

  (is= [(zoned-date-time 2018 9 1  2 3 4)
        (zoned-date-time 2018 9 2  2 3 4)
        (zoned-date-time 2018 9 3  2 3 4)
        (zoned-date-time 2018 9 4  2 3 4)]
       (range
         (zoned-date-time 2018 9 1  2 3 4)
         (zoned-date-time 2018 9 5  2 3 4)
         (Duration/ofDays 1)))

  (is= [(zoned-date-time 2018 9 1  1)
        (zoned-date-time 2018 9 1  2)
        (zoned-date-time 2018 9 1  3)
        (zoned-date-time 2018 9 1  4)]
       (range
         (zoned-date-time 2018 9 1  1 )
         (zoned-date-time 2018 9 1  5 )
         (Duration/ofHours 1))))

(dotest
  (let [start-sunday   (trunc-to-sunday-midnight (zoned-date-time 2018 9 1))
        stop-inst      (zoned-date-time 2018 9 17)
        start-instants (range start-sunday stop-inst (Duration/ofDays 7))]
    (is= start-instants
      [(zoned-date-time 2018 8 26)
       (zoned-date-time 2018 9 2)
       (zoned-date-time 2018 9 9)
       (zoned-date-time 2018 9 16)])))

(dotest
  (is= (zoned-date-time 2018 9 1) (->zoned-date-time (joda/date-time 2018 9 1)))
  (is= (zoned-date-time 2018 9 1) (->zoned-date-time (joda/date-time 2018 9 1 ,, 0 0 0)))
  (is= (zoned-date-time 2018 9 1 ,, 2 3 4) (->zoned-date-time (joda/date-time 2018 9 1 ,, 2 3 4)))

  (is (same-instant?
        (zoned-date-time 2018 9 1)
        (joda/date-time 2018 9 1)
        (->instant (zoned-date-time 2018 9 1))
        (->instant (joda/date-time 2018 9 1)) ))
  (is (same-instant?
        (zoned-date-time 2018 9 1)
        (joda/date-time 2018 9 1)
        (->zoned-date-time (zoned-date-time 2018 9 1))
        (->zoned-date-time (joda/date-time 2018 9 1)) ))
  (is (same-instant?
        (zoned-date-time 2018 9 1)
        (joda/date-time 2018 9 1)
        (->instant (->zoned-date-time (zoned-date-time 2018 9 1)))
        (->instant (->zoned-date-time (joda/date-time 2018 9 1))))) )


(dotest
  (let [lb      (zoned-date-time 2018 9 1)
        ub      (zoned-date-time 2018 9 2)
        mid     (zoned-date-time 2018 9 1, 1 2 3)

        itvl    (interval lb ub)
        itvl-o  (interval lb ub :open)
        itvl-ho (interval lb ub :half-open)
        itvl-c  (interval lb ub :closed)]
    (is= itvl itvl-ho)
    (is (interval-contains? itvl lb))
    (is (interval-contains? itvl mid))
    (isnt (interval-contains? itvl ub))

    (isnt (interval-contains? itvl-o lb))
    (is (interval-contains? itvl-o mid))
    (isnt (interval-contains? itvl-o ub))

    (is (interval-contains? itvl-c lb))
    (is (interval-contains? itvl-c mid))
    (is (interval-contains? itvl-c ub)) )

  (is= (stringify-times [:something
                         {:ambassador-id 13590,
                          :created-at    (with-zoneid zoneid-us-pacific
                                           (zoned-date-time 2018 1 2, 7))
                          :team-id       45,}
                         #{:some :more :stuff}])
    [:something
     {:ambassador-id 13590
      :created-at    "2018-01-02T15:00:00Z"
      :team-id       45}
     #{:stuff :some :more}])
  )

(dotest
  (let [now-instant-1      (instant)
        now-zdt-1          (zoned-date-time)
        now-zdt-2          (now->zdt)
        >>                 (Thread/sleep 100)
        now-instant-2      (now->instant)

        tst-interval-short (interval now-instant-1 now-instant-2)
        tst-interval-1-sec (interval (.minusSeconds now-instant-1 1) now-instant-2)

        now-millis         (.toEpochMilli now-instant-1)
        now-instant-millis (millis->instant now-millis)
        now-instant-secs   (secs->instant (quot now-millis 1000))]
    (is (interval-contains? tst-interval-short now-zdt-1))
    (is (interval-contains? tst-interval-short now-zdt-2))
    (is (interval-contains? tst-interval-1-sec now-instant-millis))
    (is (interval-contains? tst-interval-1-sec now-instant-secs))))

