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

package org.activiti.impl.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.apache.ibatis.session.SqlSession;


/** handles dirty checking of loaded objects and is resposible for 
 * applying the db specific query translations.  
 * 
 * @author Tom Baeyens
 */
public class IbatisSessionWrapper implements Session {
  
  protected SqlSession sqlSession;

  protected List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();
  protected Map<Object, LoadedObject> loadedObjects = new HashMap<Object, LoadedObject>();
  protected List<PersistentObject> deletedObjects = new ArrayList<PersistentObject>();
  protected Set<PersistentObjectId> deletedObjectIds = new HashSet<PersistentObjectId>();

  public void flush() {
  }

  public void close() {
  }
}
