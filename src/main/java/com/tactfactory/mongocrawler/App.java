/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.tactfactory.mongocrawler;

import com.mongodb.client.MongoDatabase;
import com.tactfactory.mongocrawler.database.MongoService;
import com.tactfactory.mongocrawler.managers.MongoIAManager;
import com.tactfactory.mongocrawler.managers.MongoUserManager;
import com.tactfactory.mongocrawler.utils.ScannerUtil;

public class App {
  public String getGreeting() {
    return "Hello world.";
  }

  public static void main(String[] args) throws Exception {
    System.out.println(new App().getGreeting());

    String host = "127.0.0.1";
    Integer port = 27017;
    String database = "";
    Boolean iaMode = false;
    Integer iaLoop = 3000;

    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("--")) {
        if (args[i].equals("--host")) {
          if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
            host = args[i + 1];
          } else {
            throw new Exception("--host not defined");
          }
        }
        if (args[i].equals("--port")) {
          if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
            try {
              port = Integer.parseInt(args[i + 1]);
            } catch (NumberFormatException e) {
              throw e;
            }
          } else {
            throw new Exception("--port not defined");
          }
        }
        if (args[i].equals("--db")) {
          if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
            database = args[i + 1];
          } else {
            throw new Exception("--db not defined");
          }
        }
        if (args[i].equals("--ia")) {
          iaMode = true;
        }
        if (args[i].equals("--ialoop")) {
          if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
            try {
              iaLoop = Integer.parseInt(args[i + 1]);
            } catch (NumberFormatException e) {
              throw e;
            }
          } else {
            throw new Exception("--ialoop not defined");
          }
        }
      }
    }

    MongoService service = new MongoService(host, port);
    MongoDatabase db = service.getMongoClient().getDatabase(database);

    if (iaMode) {
      new MongoIAManager(db, iaLoop).run();
    }else {
      new MongoUserManager(db).run();
    }

    System.out.println("Ended");
  }
}
