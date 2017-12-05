package com.lightbend.flights;

import akka.actor.ActorRef;

public interface EventRegistrar {

    void register(ActorRef actor);
}
