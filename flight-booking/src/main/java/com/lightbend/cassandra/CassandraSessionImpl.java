package com.lightbend.cassandra;

import akka.Done;
import akka.actor.ActorSystem;
import akka.persistence.cassandra.ConfigSessionProvider;
import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CassandraSessionImpl implements CassandraSession {

    private static final Logger log = LoggerFactory.getLogger(CassandraSessionImpl.class);

    private Session session;

    public CassandraSessionImpl() {

    }

    public CompletionStage<Done> connect(ActorSystem system, ExecutionContext ctx) {
        CompletableFuture<Session> future = new CompletableFuture<>();

        Config config = ConfigFactory.load().getConfig("cassandra-journal");

        new ConfigSessionProvider(system, config)
                .connect(ctx)
                .onComplete(session -> {
                    if (session.isSuccess()) {
                        future.complete(session.get());
                    } else {
                        future.completeExceptionally(new RuntimeException("Failed to create Cassandra session"));
                    }

                    return null;
                }, system.dispatcher());

        return future.thenApply(session -> {
            (this.session = session).getCluster().getConfiguration().getCodecRegistry().register(SimpleTimestampCodec.instance);
            return Done.getInstance();
        });
    }

    @Override
    public CompletionStage<List<Row>> execute(String query) {
        CompletableFuture<List<Row>> future = new CompletableFuture<>();

        Futures.addCallback(session.executeAsync(query), new FutureCallback<ResultSet>() {

            @Override
            public void onSuccess(ResultSet result) {
                future.complete(result.all());
            }

            @Override
            public void onFailure(Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    @Override
    public CompletionStage<List<Row>> execute(String query, Object... bind) {
        return null;
    }

    @Override
    public CompletionStage<PreparedStatement> prepare(String statement) {
        CompletableFuture<PreparedStatement> future = new CompletableFuture<>();

        Futures.addCallback(session.prepareAsync(statement), new FutureCallback<PreparedStatement>() {
            @Override
            public void onSuccess(PreparedStatement result) {
                future.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    @Override
    public CompletionStage<List<Row>> execute(BoundStatement statement) {
        CompletableFuture<List<Row>> future = new CompletableFuture<>();

        Futures.addCallback(session.executeAsync(statement), new FutureCallback<ResultSet>() {

            @Override
            public void onSuccess(ResultSet result) {
                future.complete(result.all());
            }

            @Override
            public void onFailure(Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }
}
