package com.tactfactory.mongocrawler.managers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tactfactory.mongocrawler.utils.ScannerUtil;

public class MongoUserManager {

  private final MongoDatabase db;

  public MongoUserManager(final MongoDatabase db) {
    this.db = db;
  }

  public void run() {
    String collection = this.selectCollection();
    Integer option = this.selectionCollectionOption();

    switch (option) {
    case 1:
        this.findIncollection(collection);
      break;
    case 2:
        this.insertInCollection(collection);
      break;

    default:
      break;
    }
  }

  private void insertInCollection(String collection) {
    // TODO Auto-generated method stub
    System.out.println("insert");
  }

  private int loop;
  private void findIncollection(String collection) {
    final FindIterable<Document> documents = this.db.getCollection(collection).find();

    final Map<Integer, String> fields = new HashMap<>();
    final Map<String, String> fieldsType = new HashMap<>();
    final List<String> fieldsChoice = new ArrayList<>();

    loop = 1;

    documents.forEach(x -> x.keySet()
        .forEach(y -> {
          if(!fields.containsValue(y)) {
            String typeString = x.get(y) == null ? "null" : x.get(y).getClass().getName();
            if (!typeString.equals(ArrayList.class.getName())) {
              fieldsType.put(y, typeString);
              fields.put(loop,y);
              fieldsChoice.add(String.format("%s : %s", loop, y));
              MongoUserManager.this.loop++;
            }else {
              //System.out.println("Array");
            }
          }
        }));
    String choice = ScannerUtil.getInstance().selectStringFromIntChoice("Sélection un champ", fieldsChoice.toArray(),fields);

    System.out.println(String.format("Insérer une valeur pour %s", choice));

    Class<?> cls = null;
    try {
      cls = Class.forName(fieldsType.get(choice));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      cls = String.class;
    }

    Bson filter = null;
    if (cls == String.class) {
      filter = Filters.eq(choice, ScannerUtil.getInstance().inputString());
    }else if(cls == Integer.class) {
      filter = Filters.eq(choice, ScannerUtil.getInstance().inputInt());
    }

    FindIterable<Document> finded = this.db.getCollection(collection).find(filter);

    for (Document document : finded) {
      System.out.println(document.toJson());
    }

    System.out.println("find");
  }

  private Integer selectionCollectionOption() {
    final List<String> datas = new ArrayList<>();
    datas.add("1 : rechercher");
    datas.add("2 : insérer");
    return ScannerUtil.getInstance().selectInt("Sélectionner une option", datas.toArray(), 1, 2);
  }

  private String selectCollection() {
    final List<String> datas = new ArrayList<>();
    final Map<Integer, String> choicesDatas = new HashMap<Integer, String>();
    int loop = 1;
    for (String collectionName : db.listCollectionNames()) {
      choicesDatas.put(loop, collectionName);
      datas.add(String.format("%s : %s", loop, collectionName));
      loop++;
    }

    String choicedCollection = ScannerUtil.getInstance().selectStringFromIntChoice("Sélectionner une collection",
        datas.toArray(), choicesDatas);
    System.out.println(String.format("Now in %s collection", choicedCollection));

    return choicedCollection;
  }
}
