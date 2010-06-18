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
package org.activiti.test.persistence.inmemorylistsession;

/**
 * An interface which allows the {@link InMemoryListBasedPersistenceSession}
 *  to notify an object about when it is stored and fetched
 */
public interface PersistenceTimingAware {
  /** Called when a find* method picks this */
  public void foundAt(long time);
  public void deletedAt(long time);
  public void insertedAt(long time);
}
