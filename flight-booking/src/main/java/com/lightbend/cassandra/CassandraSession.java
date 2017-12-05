package com.lightbend.cassandra;

import akka.Done;
import akka.actor.ActorSystem;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import scala.concurrent.ExecutionContext;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface CassandraSession {

    CompletionStage<Done> connect(ActorSystem system, ExecutionContext ctx);

    CompletionStage<List<Row>> execute(String query);

    CompletionStage<List<Row>> execute(String query, Object... bind);

    CompletionStage<PreparedStatement> prepare(String statement);

    CompletionStage<List<Row>> execute(BoundStatement statement);
}
