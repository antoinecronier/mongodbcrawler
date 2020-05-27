package com.tactfactory.mongocrawler.database;

import java.util.Arrays;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoService {

  private final MongoClient mongoClient;

  public MongoClient getMongoClient() {
    return mongoClient;
  }

  public MongoService() {
    this("127.0.0.1", 27017);
  }

  public MongoService(final Integer port) {
    this("127.0.0.1", port);
  }

  public MongoService(final String host) {
    this(host, 27017);
  }

  public MongoService(final String host, final Integer port) {
    this.mongoClient = MongoClients.create(
        MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                .build());
  }
}
