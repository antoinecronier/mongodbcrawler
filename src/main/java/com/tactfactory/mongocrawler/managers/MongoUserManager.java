package com.tactfactory.mongocrawler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
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

  private void findIncollection(String collection) {
    // TODO Auto-generated method stub
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
