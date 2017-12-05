# flight-booking-akka

## Quickstart

Easiest way to get started is running Kafka and Cassandra via the *-server projects provided and then starting the app itself. 

You can do this via your favourite IDE or from the terminal by running the following commands each in a different terminal window/session

```
sbt 'kafka-server/run'
sbt 'cassandra-server/run'
```

Now Kafka and Cassandra are running you can run the Akka cluster.

Either run 3 nodes in the same JVM by default by simply running `com.lightbend.flights.FlightBookin` as the main class with no arguments.

Or, run a single node by passing the port number you want the node to listen on

```
sbt 'flight-booking/run 3551'
sbt 'flight-booking/run 3552'
sbt 'flight-booking/run 3553'

...you get the idea...
```

## Integration Tests (Postman)

Once you're up and running there's a Postman collection that'll run all the available API calls for you at https://www.getpostman.com/collections/c24eefc199ebbfd1b9ec

## Running in Kubernetes (INCOMPLETE)

```sh
# Use minikube locally
(minikube delete || true) &>/dev/null && minikube start --memory 8192 && eval $(minikube docker-env)

# Bring up a Cassandra service
kubectl create -f deploy/kubernetes/resources/cassandra && deploy/kubernetes/scripts/kubectl-wait-for-pods && kubectl exec cassandra-0 -- nodetool status
```

Now you can run the `FlightBooking` app from within your IDE in a single JVM.

