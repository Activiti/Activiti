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
package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.db.HasRevision;

/**
 * @author Tom Baeyens
 * @author Arkadiy Gornovoy
 */
public interface UserEntity extends User, Entity, HasRevision {

  Picture getPicture();

  void setPicture(Picture picture);

  String getId();

  void setId(String id);

  String getFirstName();

  void setFirstName(String firstName);

  String getLastName();

  void setLastName(String lastName);

  String getEmail();

  void setEmail(String email);

  String getPassword();

  void setPassword(String password);

  boolean isPictureSet();

  ByteArrayRef getPictureByteArrayRef();
  
}
