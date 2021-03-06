package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Option;

import java.util.UUID;

public class Flights extends AbstractActor {

    public static Props props() {
        return Props.create(Flights.class, () -> new Flights());
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef shards;

    static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.HashCodeMessageExtractor(100) {

        @Override
        public String entityId(Object message) {

            if (message instanceof FlightCommand) {
                return ((FlightCommand) message).getFlightId().toString();
            }

            if (message instanceof FlightCommand.AddFlight) {
                return UUID.randomUUID().toString();
            }

            throw new RuntimeException("expecting message of type 'FlightCommand' but was " + message.getClass().getName());
        }
    };

    public Flights() {
        ActorSystem system = getContext().getSystem();
        Option<String> roleOption = Option.none();
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);

        shards = ClusterSharding.get(system)
                .start(
                        "Flights",
                        FlightEntity.props(),
                        settings,
                        messageExtractor);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FlightCommand.class, cmd -> shards.forward(cmd, getContext()))
                .match(FlightCommand.AddFlight.class, cmd -> shards.forward(cmd, getContext()))
                .matchAny(o -> log.warning("i don't know what to do with {}", o))
                .build();
    }
}
