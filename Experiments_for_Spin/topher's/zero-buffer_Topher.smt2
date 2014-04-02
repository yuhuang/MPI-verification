
(declare-datatypes (T1 T2 T3 T4 T5)
 ((Recv (mk-recv (match T1) (ep T2) (var T3) (event T4) (nearestwait T5)))))
(declare-datatypes (T1 T2 T3 T4)
((Send (mk-send (match T1) (ep T2) (value T3) (event T4)))))
(define-fun HB ((a Int) (b Int)) Bool
  (if(< a b)
 true
 false))

(define-fun HB* ((a Int) (b Int)) Bool
  (if(= a (- b 1))
 true
 false))

(define-fun MATCH ((r (Recv Int Int Int Int Int)) (s (Send Int Int Int Int))) Bool
(if(and (= (ep r) (ep s)) (= (var r) (value s))
(HB* (event s) (event r)) (= (match r) (event s))
(= (match s) (event r)))
true
false
)
)

(declare-const T0_0 Int)
(declare-const T0_1 Int)
(declare-const T0_2 Int)
(declare-const T0_3 Int)
(declare-const T0_4 Int)
(declare-const T1_0 Int)
(declare-const T1_1 Int)
(declare-const T1_2 Int)
(declare-const T1_3 Int)
(declare-const T2_0 Int)
(declare-const T2_1 Int)
(declare-const T2_2 Int)
(declare-const T2_3 Int)
(declare-const a Int)
(declare-const b Int)
(declare-const c Int)

(declare-const recvT0_0 (Recv Int Int Int Int Int))
(assert (and (= (ep recvT0_0) 0) (= (event recvT0_0) T0_0) (= (var recvT0_0) a)))
(assert (= (nearestwait recvT0_0) T0_1))
(declare-const recvT0_2 (Recv Int Int Int Int Int))
(assert (and (= (ep recvT0_2) 0) (= (event recvT0_2) T0_2) (= (var recvT0_2) b)))
(assert (= (nearestwait recvT0_2) T0_3))
(declare-const recvT1_0 (Recv Int Int Int Int Int))
(assert (and (= (ep recvT1_0) 1) (= (event recvT1_0) T1_0) (= (var recvT1_0) c)))
(assert (= (nearestwait recvT1_0) T1_1))

(declare-const sendT1_2 (Send Int Int Int Int))
(assert (and (= (ep sendT1_2 ) 0) (= (event sendT1_2) T1_2) (= (value sendT1_2) 1)))
(declare-const sendT2_0 (Send Int Int Int Int))
(assert (and (= (ep sendT2_0) 0) (= (event sendT2_0) T2_0) (= (value sendT2_0) 4)))
(declare-const sendT2_2 (Send Int Int Int Int))
(assert (and (= (ep sendT2_2) 1) (= (event sendT2_2) T2_2) (= (value sendT2_2) 111)))


(assert (and
(HB T0_0 T0_1) (HB T0_0 T0_2) (HB T0_2 T0_3) (HB T0_3 T0_4) (HB T1_0 T1_1) (HB T1_1 T1_2) (HB T2_0 T2_2)))


(assert (and 
(or (MATCH recvT0_0 sendT1_2) (MATCH recvT0_0 sendT2_0))
(or (MATCH recvT0_2 sendT1_2) (MATCH recvT0_2 sendT2_0))
(or (MATCH recvT1_0 sendT2_2))
))

(assert (= a 1))

(check-sat)
(get-model)

