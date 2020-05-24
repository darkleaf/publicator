(ns publicator.web.routes-test
  (:require
   [clojure.test :as t]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.list :as user.list]
   [publicator.core.use-cases.interactors.user.log-in :as user.log-in]
   [publicator.core.use-cases.interactors.user.log-out :as user.log-out]
   [publicator.core.use-cases.interactors.user.register :as user.register]
   [publicator.core.use-cases.interactors.user.update :as user.update]
   [publicator.web.routes :as routes]
   [reitit.ring]))

(defn handler []
  (-> (routes/routes)
      (reitit.ring/router)
      (reitit.ring/ring-handler)))

(t/deftest user-log-in-get
  (let [request          {:request-method :get
                          :uri            "/user/log-in"}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.log-in/form :args])]
    (t/is (= user.log-in/form process))
    (t/is (apply args-predicate args))))

(t/deftest user-log-in-post
  (let [request          {:request-method :post
                          :uri            "/user/log-in"
                          :body           (agg/allocate)}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.log-in/process :args])]
    (t/is (= user.log-in/process process))
    (t/is (apply args-predicate args))))


(t/deftest user-log-out-post
  (let [request          {:request-method :post
                          :uri            "/user/log-out"}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.log-out/process :args])]
    (t/is (= user.log-out/process process))
    (t/is (apply args-predicate args))))


(t/deftest user-register-get
  (let [request          {:request-method :get
                          :uri            "/user/register"}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.register/form :args])]
    (t/is (= user.register/form process))
    (t/is (apply args-predicate args))))

(t/deftest user-register-post
  (let [request          {:request-method :post
                          :uri            "/user/register"
                          :body           (agg/allocate)}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.register/process :args])]
    (t/is (= user.register/process process))
    (t/is (apply args-predicate args))))


(t/deftest user-list-get
  (let [request          {:request-method :get
                          :uri            "/user/list"}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.list/process :args])]
    (t/is (= user.list/process process))
    (t/is (apply args-predicate args))))


(t/deftest user-update-get
  (let [request          {:request-method :get
                          :uri            "/user/update/1"}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.update/form :args])]
    (t/is (= user.update/form process))
    (t/is (apply args-predicate args))))

(t/deftest user-update-post
  (let [request          {:request-method :post
                          :uri            "/user/update/1"
                          :body           (agg/allocate)}
        [process & args] ((handler) request)
        args-predicate   (get-in @contracts/registry [`user.update/process :args])]
    (t/is (= user.update/process process))
    (t/is (apply args-predicate args))))
