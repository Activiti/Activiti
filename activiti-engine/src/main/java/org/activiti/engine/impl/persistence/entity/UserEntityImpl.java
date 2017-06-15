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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.Picture;
import org.activiti.engine.impl.db.HasRevision;

/**
 * @author Tom Baeyens
 * @author Arkadiy Gornovoy
 * @author Joram Barrez
 */
public class UserEntityImpl extends AbstractEntity implements UserEntity, Serializable, HasRevision {

  private static final long serialVersionUID = 1L;

  protected String firstName;
  protected String lastName;
  protected String email;
  protected String password;

  protected ByteArrayRef pictureByteArrayRef;

  public UserEntityImpl() {
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("firstName", firstName);
    persistentState.put("lastName", lastName);
    persistentState.put("email", email);
    persistentState.put("password", password);
    
    if (pictureByteArrayRef != null) {
      persistentState.put("pictureByteArrayId", pictureByteArrayRef.getId());
    }
    
    return persistentState;
  }

  public Picture getPicture() {
    if (pictureByteArrayRef != null && pictureByteArrayRef.getId() != null) {
      return new Picture(pictureByteArrayRef.getBytes(), pictureByteArrayRef.getName());
    }
    return null;
  }

  public void setPicture(Picture picture) {
    if(picture != null) {
      savePicture(picture);
    } else {
      deletePicture();
    }      
  }

  protected void savePicture(Picture picture) {
    if (pictureByteArrayRef != null) {
      pictureByteArrayRef = new ByteArrayRef();
    }
    pictureByteArrayRef.setValue(picture.getMimeType(), picture.getBytes());
  }
  
  protected void deletePicture() {
    if (pictureByteArrayRef != null) {
      pictureByteArrayRef.delete();
    }
  }
  
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isPictureSet() {
    return pictureByteArrayRef != null && pictureByteArrayRef.getId() != null;
  }

  public ByteArrayRef getPictureByteArrayRef() {
    return pictureByteArrayRef;
  }
  
}
