package com.lightbend.flights;

import akka.persistence.journal.Tagged;
import akka.persistence.journal.WriteEventAdapter;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TaggingEventAdapter implements WriteEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(TaggingEventAdapter.class);

    @Override
    public String manifest(Object event) {
        return "";
    }

    @Override
    public Object toJournal(Object event) {

        if (event instanceof FlightEvent) {
            Set<String> tags = Sets.newHashSet();
            tags.add("flight");

            return new Tagged(event, tags);
        }

        return event;
    }
}
