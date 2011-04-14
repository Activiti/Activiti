/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.persistence;

import java.util.Set;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;


/**
 * @author Tom Baeyens
 */
public abstract class Persistence {
  
  private static Logger log = Logger.getLogger(Persistence.class.getName());

  static Mongo mongo = null;
  static DB db = null;
  static Users users = null; 
  static Tasks tasks = null; 
  
  static {
    try {
      mongo = new Mongo();
      db = mongo.getDB( "vibes" );
      users = new Users(db.getCollection("users"));
      tasks = new Tasks(db.getCollection("tasks"));
    } catch (Exception e) {
      throw new RuntimeException("couldn't open mongodb connection: "+e.getMessage(), e);
    }
  }

  public static Users getUsers() {
    return users;
  }

  public static Tasks getTasks() {
    return tasks;
  }

  public static void clean() {
    log.info("cleaning database");
    Set<String> collectionNames = db.getCollectionNames();
    for (String collectionName : collectionNames) {
      log.info(collectionName+" -----------------------");
      DBCollection collection = db.getCollection(collectionName);
      DBCursor cursor = collection.find();
      while (cursor.hasNext()) {
        DBObject dbObject = cursor.next();
        log.info("  "+dbObject);
        collection.remove(dbObject);
      }
    }
  }
}
