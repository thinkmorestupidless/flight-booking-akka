import com.typesafe.sbt.packager.docker._

organization in ThisBuild := "less.stupid"
version in ThisBuild := "1.0.3-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.2"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val akkaVersion = "2.5.6"

lazy val root = (project in file("."))
  .settings(name := "flight-booking")
  .aggregate(`kafka-server`, `cassandra-server`, `flight-booking-akka`)
  .settings(commonSettings: _*)

lazy val `kafka-server` = (project in file("kafka-server"))
.settings(
  mainClass in Compile := Some("com.lightbend.kafka.KafkaLauncher"),
  libraryDependencies ++= Seq(
    "org.apache.kafka" %% "kafka" % "0.11.0.0",
    "org.apache.curator" % "curator-framework" % "2.10.0",
    "org.apache.curator" % "curator-test" % "2.10.0",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.opengamma.strata" % "strata-collect" % "1.4.2"
  )
)

lazy val `cassandra-server` = (project in file("cassandra-server"))
.settings(
  mainClass in Compile := Some("com.lightbend.cassandra.CassandraLauncher"),
  libraryDependencies ++= Seq(
    "org.cassandraunit" % "cassandra-unit" % "3.3.0.2",
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.2.0",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.opengamma.strata" % "strata-collect" % "1.4.2"
  )
)

lazy val `flight-booking-akka` = (project in file("flight-booking"))
.enablePlugins(JavaAppPackaging)
.settings(
  mainClass in Compile := Some("com.lightbend.flights.FlightBooking"),
  dockerEntrypoint ++= Seq(
    """-Dcassandra-journal.contact-points.0="$CASSANDRA_SERVICE_NAME"""",
    """-Dakka.kafka.producer.kafka-clients.bootstrap-servers="kafka-0.broker.kafka.svc.cluster.local:9092"""",
    """-Dakka.kafka.consumer.kafka-clients.bootstrap-servers="kafka-0.broker.kafka.svc.cluster.local:9092"""",
    """-Dhttp.address="$FLIGHTSSERVICE_BIND_IP"""",
    """-Dhttp.port="$FLIGHTSSERVICE_BIND_PORT"""",
    """-Dakka.actor.provider=cluster""",
    """-Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"""",
    """-Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT"""",
    """$(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://$AKKA_ACTOR_SYSTEM_NAME@$NODE"; I=$(expr $I + 1); done)""",
    "-Dakka.io.dns.resolver=async-dns",
    "-Dakka.io.dns.async-dns.resolve-srv=true",
    "-Dakka.io.dns.async-dns.resolv-conf=on "
  ),
  dockerCommands := dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  },
  dockerRepository := Some("thinkmorestupidless"),
  dockerExposedPorts := Seq(9000, 2551),
  dockerUpdateLatest := true,
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-cluster" % "2.5.6",
    "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.6",
    "com.typesafe.akka" %% "akka-cluster-sharding" % "2.5.6",
    "com.typesafe.akka" %% "akka-cluster-metrics" % "2.5.6",
    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-http-jackson" % "10.0.10",
    "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.56" exclude("com.google.guava", "guava"),
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
    "com.datastax.cassandra" % "cassandra-driver-extras" % "3.1.4",
    "org.projectlombok" % "lombok" % "1.16.10" % "compile",
    "com.opengamma.strata" % "strata-collect" % "1.4.2",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "com.google.guava"  % "guava" % "23.0",
    "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1",
    "junit" % "junit" % "4.12" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % "test",
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.58" % "test",
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1" % "test"
  )
)



licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"


def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)
