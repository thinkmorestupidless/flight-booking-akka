# flight-booking-akka

## Quickstart

Easiest way to get started is running Kafka and Cassandra via the *-server projects provided and then starting the app itself. 

You can do this via your favourite IDE or from the terminal by running the following commands each in a different terminal window/session

```
sbt 'kafka-server/run'
sbt 'cassandra-server/run'
```

Now Kafka and Cassandra are running you can run the Akka cluster.

Either:
1. run 3 nodes in the same JVM by default by simply running `com.lightbend.flights.FlightBooking` as the main class with no arguments.

OR

2. run a single node by passing the port number you want the node to listen on

```
sbt 'flight-booking/run 3551'
sbt 'flight-booking/run 3552'
sbt 'flight-booking/run 3553'

...you get the idea...
```

## Integration Tests (Postman)

Once you're up and running there's a Postman collection that'll run all the available API calls for you at https://www.getpostman.com/collections/c24eefc199ebbfd1b9ec

## Running in DC/OS

> Note: Only tested in the local development (Vagrant) cluster

### Cluster Setup

If you just want to run this locally then follow the instructions for setting up a [local DC/OS cluster with Vagrant](https://github.com/dcos/dcos-vagrant)

> If you're running MacOSX 10.13.* latest versions of Vagrant and VirtualBox don't work (for me, at least) - i had success with Vagrant 1.9.3 and VirtualBox 5.1.30

### Deployment



## Running in Kubernetes (INCOMPLETE)

### Local (Minikube)

Firstly, you'll need to get Kubernetes running locally, along with the other services required by the application; Cassandra and Kafka.

```sh
# Use minikube locally
(minikube delete || true) &>/dev/null && minikube start --memory 8192 && eval $(minikube docker-env)
```

Now the environment is ready you can create the dockerized version of the service and deploy it into the Kubernetes cluster.

```
# dockerize the service and publish to the local docker registry
sbt docker:publishLocal

# now use the helpful 'install' script to actually get it running in the cluster
# here, we're setting up Cassandra and Nginx as well as the service itself.
./deploy/kubernetes/scripts/install --deploy --minikube

# the output of the script will give you the endpoints you can use in your Postman environment to run the tests.
```

