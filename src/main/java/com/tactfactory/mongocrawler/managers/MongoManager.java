package com.tactfactory.mongocrawler.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tactfactory.mongocrawler.configurations.Operators;
import com.tactfactory.mongocrawler.utils.ScannerUtil;

public abstract class MongoManager {

  protected final MongoDatabase db;
  private Integer loop;

  public MongoManager(final MongoDatabase db) {
    this.db = db;
  }

  protected void ExtractDocumentsFields(final FindIterable<Document> documents, final Map<Integer, String> fields,
      final Map<String, String> fieldsType, final List<String> fieldsChoice) {
    loop = 0;
    documents.forEach(x -> x.keySet().forEach(y -> {
      if (!fields.containsValue(y)) {
        String typeString = x.get(y) == null ? "null" : x.get(y).getClass().getName();
        if (!typeString.equals(ArrayList.class.getName())) {
          fieldsType.put(y, typeString);
          fields.put(loop, y);
          fieldsChoice.add(String.format("%s : %s", loop, y));
          MongoManager.this.loop++;
        } else {
          // System.out.println("Array");
        }
      }
    }));
  }

  protected Bson ActionsForFieldAndOperator(final Map<String, String> fieldsType, String choice, int choiceOperator) {
    Class<?> cls = null;
    try {
      cls = Class.forName(fieldsType.get(choice));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      cls = String.class;
    }

    Bson filter = null;
    if (cls == String.class) {
      switch (choiceOperator) {
      case 0:
        filter = Filters.eq(choice, ScannerUtil.getInstance().inputString());
        break;
      case 5:
        filter = Filters.ne(choice, ScannerUtil.getInstance().inputString());
        break;

      default:
        System.out.println(String.format("Opération non autorisé pour l'opérateur %s",
            Operators.getDisplayList().get(choiceOperator)));
        break;
      }

    } else if (cls == Integer.class) {
      switch (choiceOperator) {
      case 0:
        filter = Filters.eq(choice, ScannerUtil.getInstance().inputInt());
        break;

      case 1:
        filter = Filters.gt(choice, ScannerUtil.getInstance().inputInt());
        break;

      case 2:
        filter = Filters.lt(choice, ScannerUtil.getInstance().inputInt());
        break;

      case 3:
        filter = Filters.gte(choice, ScannerUtil.getInstance().inputInt());
        break;

      case 4:
        filter = Filters.lte(choice, ScannerUtil.getInstance().inputInt());
        break;

      case 5:
        filter = Filters.ne(choice, ScannerUtil.getInstance().inputInt().intValue());
        break;

      default:
        System.out.println(String.format("Opération non autorisé pour l'opérateur %s",
            Operators.getDisplayList().get(choiceOperator)));
        break;
      }
    }
    return filter;
  }

  protected void findOneAndUpdate(String collection, FindIterable<Document> documents, final BsonDocument value) {
    documents.map(x -> x.get("_id"))
        .forEach(y -> db.getCollection(collection).findOneAndUpdate(Filters.eq("_id", y), value));
  }

  public abstract void run() throws InterruptedException;
}
