package com.lightbend.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraLauncher {

    private static final Logger log = LoggerFactory.getLogger(CassandraLauncher.class);

    public static void main(String[] args) throws Exception {
        log.info("starting local Cassandra server");

        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }
}
