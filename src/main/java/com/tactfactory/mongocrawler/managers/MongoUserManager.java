package com.tactfactory.mongocrawler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tactfactory.mongocrawler.configurations.Operators;
import com.tactfactory.mongocrawler.utils.ScannerUtil;

public class MongoUserManager {

  private final MongoDatabase db;

  public MongoUserManager(final MongoDatabase db) {
    this.db = db;
  }

  public void run() {
    while (true) {
      String collection = this.selectCollection();
      Integer option = this.selectionCollectionOption();

      switch (option) {
      case 1:
        FindIterable<Document> documents = this.findIncollection(collection);
        if (documents.iterator().hasNext()) {
          String documentId;
          Integer searchOption = this.selectionSearchOption();
          switch (searchOption) {
          case 1:
            documents.map(x -> x.get("_id")).forEach(y -> db.getCollection(collection).deleteOne(Filters.eq("_id", y)));
            break;
          case 2:
            documentId = selectOneDocumentId(documents);
            db.getCollection(collection).deleteOne(Filters.eq("_id", documentId));
            break;
          case 3:
            this.updateAllForField(collection, documents);
            break;
          case 4:
            documentId = selectOneDocumentId(documents);
            this.updateOneForField(collection, documents, documentId);
            break;
          }
        }
        break;
      case 2:
        this.insertInCollection(collection);
        break;
      }
    }
  }

  private void updateOneForField(String collection, FindIterable<Document> documents, String documentId) {
    final Map<Integer, String> fields = new HashMap<>();
    final Map<String, String> fieldsType = new HashMap<>();
    final List<String> fieldsChoice = new ArrayList<>();

    String field = this.selectField(documents, fields, fieldsType, fieldsChoice);

    Class<?> cls = null;
    try {
      cls = Class.forName(fieldsType.get(field));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      cls = String.class;
    }

    System.out.println(String.format("Insérer une valeur pour %s ", field));

    BsonDocument value = null;
    if (cls == String.class) {
      value = new BsonDocument("$set",
          new BsonDocument(field, new BsonString(ScannerUtil.getInstance().inputString())));
    } else if (cls == Integer.class) {
      value = new BsonDocument("$set", new BsonDocument(field, new BsonInt32(ScannerUtil.getInstance().inputInt())));
    }

    db.getCollection(collection).findOneAndUpdate(Filters.eq("_id", documentId), value);
  }

  private void updateAllForField(String collection, FindIterable<Document> documents) {
    final Map<Integer, String> fields = new HashMap<>();
    final Map<String, String> fieldsType = new HashMap<>();
    final List<String> fieldsChoice = new ArrayList<>();

    String field = this.selectField(documents, fields, fieldsType, fieldsChoice);

    Class<?> cls = null;
    try {
      cls = Class.forName(fieldsType.get(field));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      cls = String.class;
    }

    System.out.println(String.format("Insérer une valeur pour %s ", field));

    BsonDocument value = null;
    if (cls == String.class) {
      value = new BsonDocument("$set",
          new BsonDocument(field, new BsonString(ScannerUtil.getInstance().inputString())));
    } else if (cls == Integer.class) {
      value = new BsonDocument("$set", new BsonDocument(field, new BsonInt32(ScannerUtil.getInstance().inputInt())));
    }

    this.findOneAndUpdate(collection, documents, value);
  }

  private void findOneAndUpdate(String collection, FindIterable<Document> documents, final BsonDocument value) {
    documents.map(x -> x.get("_id"))
        .forEach(y -> db.getCollection(collection).findOneAndUpdate(Filters.eq("_id", y), value));
  }

  private void insertInCollection(String collection) {
    ScannerUtil.getInstance().inputString();

    FindIterable<Document> documents = this.db.getCollection(collection).find();
    if (documents.iterator().hasNext()) {
      final Map<Integer, String> fields = new HashMap<>();
      final Map<String, String> fieldsType = new HashMap<>();
      final List<String> fieldsChoice = new ArrayList<>();

      this.ExtractDocumentsFields(documents, fields, fieldsType, fieldsChoice);

      Document newDocument = new Document();

      for (String field : fields.values()) {
        Class<?> cls = null;
        try {
          cls = Class.forName(fieldsType.get(field));
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
          cls = String.class;
        }

        System.out
            .println(String.format("Insérer une valeur pour %s de type %s ou null pour passer", field, cls.getName()));

        String userValue = ScannerUtil.getInstance().inputString();
        if (!userValue.equals("null")) {
          if (cls == String.class) {
            newDocument.append(field, new BsonString(userValue));
          } else if (cls == Integer.class) {
            newDocument.append(field, new BsonInt32(Integer.parseInt(userValue)));
          }
        }
      }

      this.db.getCollection(collection).insertOne(newDocument);

    } else {
      System.out.println("il doit exister un premier document type dans la collection.");
    }
    System.out.println("insert");
  }

  private int loop;

  private FindIterable<Document> findIncollection(String collection) {
    FindIterable<Document> result = null;
    final FindIterable<Document> documents = this.db.getCollection(collection).find();

    final Map<Integer, String> fields = new HashMap<>();
    final Map<String, String> fieldsType = new HashMap<>();
    final List<String> fieldsChoice = new ArrayList<>();

    String choice = this.selectField(documents, fields, fieldsType, fieldsChoice);

    int choiceOperator = ScannerUtil.getInstance().selectInt("Sélection un opérateur",
        Operators.getDisplayChoices().toArray(), 0, Operators.getDisplayChoices().size() - 1);

    System.out.println(String.format("Insérer une valeur pour %s avec l'opérateur %s", choice,
        Operators.getDisplayList().get(choiceOperator)));

    Bson filter = this.ActionsForFieldAndOperator(fieldsType, choice, choiceOperator);

    result = this.db.getCollection(collection).find(filter);

    for (Document document : result) {
      System.out.println(document.toJson());
    }

    System.out.println("find");
    return result;
  }

  private String selectField(final FindIterable<Document> documents, final Map<Integer, String> fields,
      final Map<String, String> fieldsType, final List<String> fieldsChoice) {
    loop = 1;

    this.ExtractDocumentsFields(documents, fields, fieldsType, fieldsChoice);

    String choice = ScannerUtil.getInstance().selectStringFromIntChoice("Sélection un champ", fieldsChoice.toArray(),
        fields);
    return choice;
  }

  private Bson ActionsForFieldAndOperator(final Map<String, String> fieldsType, String choice, int choiceOperator) {
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

  private void ExtractDocumentsFields(final FindIterable<Document> documents, final Map<Integer, String> fields,
      final Map<String, String> fieldsType, final List<String> fieldsChoice) {
    documents.forEach(x -> x.keySet().forEach(y -> {
      if (!fields.containsValue(y)) {
        String typeString = x.get(y) == null ? "null" : x.get(y).getClass().getName();
        if (!typeString.equals(ArrayList.class.getName())) {
          fieldsType.put(y, typeString);
          fields.put(loop, y);
          fieldsChoice.add(String.format("%s : %s", loop, y));
          MongoUserManager.this.loop++;
        } else {
          // System.out.println("Array");
        }
      }
    }));
  }

  private String selectOneDocumentId(FindIterable<Document> documents) {
    final List<String> datas = new ArrayList<>();
    final Map<Integer, String> choicesDatas = new HashMap<Integer, String>();
    int loop = 1;
    for (Document document : documents) {
      choicesDatas.put(loop, document.get("_id").toString());
      datas.add(String.format("%s : %s", loop, document.toJson()));
      loop++;
    }

    return ScannerUtil.getInstance().selectStringFromIntChoice("Sélectionner un document", datas.toArray(),
        choicesDatas);
  }

  private Integer selectionSearchOption() {
    final List<String> datas = new ArrayList<>();
    datas.add("1 : supprimer les documents");
    datas.add("2 : supprimer un document");
    datas.add("3 : modifier les documents");
    datas.add("4 : modifier un document");
    return ScannerUtil.getInstance().selectInt("Sélectionner une option", datas.toArray(), 1, 4);
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
