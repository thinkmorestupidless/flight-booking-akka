# flight-booking-akka

## Quickstart

Easiest way to get started is running Kafka and Cassandra via the *-server projects provided and then starting the app itself. 

You can do this via your favourite IDE or from the terminal with

```


```

## Running in Kubernetes

```sh
# Use minikube locally
(minikube delete || true) &>/dev/null && minikube start --memory 8192 && eval $(minikube docker-env)

# Bring up a Cassandra service
kubectl create -f deploy/kubernetes/resources/cassandra && deploy/kubernetes/scripts/kubectl-wait-for-pods && kubectl exec cassandra-0 -- nodetool status
```

Now you can run the `FlightBooking` app from within your IDE in a single JVM.

## Integration Tests (Postman)

There's a shared Postman collection https://www.getpostman.com/collections/c24eefc199ebbfd1b9ec