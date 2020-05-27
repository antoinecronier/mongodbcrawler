package com.tactfactory.mongocrawler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.github.javafaker.Faker;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BsonField;

public class MongoIAManager extends MongoManager {

  private Integer iaLoop;
  private final List<String> collections = new ArrayList<>();

  public MongoIAManager(MongoDatabase db, Integer iaLoop) {
    super(db);

    this.iaLoop = iaLoop;

    for (String collectionName : db.listCollectionNames()) {
      collections.add(collectionName);
    }
  }

  @Override
  public void run() throws InterruptedException {
    Runnable runnable = new Runnable() {
      public void run() {
        MongoIAManager.this.fakerDocumentGenerator();
      }
    };

    int i = 0;
    while (i < 10000) {
      runnable.run();
      Thread.sleep(this.iaLoop * 1000);
      i++;
    }

//    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//    executor.scheduleAtFixedRate(runnable, 0, this.iaLoop, TimeUnit.SECONDS);
  }

  private void fakerDocumentGenerator() {
    // Select collection.
    String collection = collections.get(Faker.instance().number().numberBetween(0, collections.size() - 1));

    // Get documents.
    FindIterable<Document> documents = db.getCollection(collection).find();
    final Map<Integer, String> fields = new HashMap<>();
    final Map<String, String> fieldsType = new HashMap<>();
    final List<String> fieldsChoice = new ArrayList<>();

    this.ExtractDocumentsFields(documents, fields, fieldsType, fieldsChoice);

    // Select and hydrate all fields.
    Document newDocument = new Document();

    for (Entry<String, String> entry : fieldsType.entrySet()) {
      Class<?> cls = null;
      try {
        cls = Class.forName(entry.getValue());
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        cls = String.class;
      }

      if (cls == String.class) {
        if (entry.getKey().equals("_id")) {
          newDocument.append(entry.getKey(), new BsonString(UUID.randomUUID().toString()));
        } else {
          StringBuilder builder = new StringBuilder();
          List<String> strings = Faker.instance().lorem().words(Faker.instance().number().numberBetween(1, 5));
          for (String string : strings) {
            builder.append(string);
          }
          newDocument.append(entry.getKey(), new BsonString(builder.toString()));
        }
      } else if (cls == Integer.class) {
        newDocument.append(entry.getKey(), new BsonInt32(Faker.instance().number().numberBetween(1, Integer.MAX_VALUE)));
      } else if (cls == Boolean.class) {
        newDocument.append(entry.getKey(), new BsonBoolean(Faker.instance().bool().bool()));
      } else if (cls == Double.class) {
        newDocument.append(entry.getKey(), new BsonDouble(Faker.instance().number().randomDouble(20, -Integer.MAX_VALUE, Integer.MAX_VALUE)));
      }
    }

    // Insert.
    System.out.println(String.format("Insert %s", newDocument.toJson()));
    this.db.getCollection(collection).insertOne(newDocument);
  }
}
