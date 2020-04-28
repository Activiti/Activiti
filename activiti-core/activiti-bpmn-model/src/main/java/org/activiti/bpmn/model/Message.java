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
package org.activiti.bpmn.model;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;

public class Message extends BaseElement {

  protected String name;
  protected String itemRef;

  private Message(Builder builder) {
    this.id = builder.id;
    this.xmlRowNumber = builder.xmlRowNumber;
    this.xmlColumnNumber = builder.xmlColumnNumber;
    this.extensionElements = builder.extensionElements;
    this.attributes = builder.attributes;
    this.name = builder.name;
    this.itemRef = builder.itemRef;
  }

  public Message() {
  }

  public Message(String id, String name, String itemRef) {
    this.id = id;
    this.name = name;
    this.itemRef = itemRef;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getItemRef() {
    return itemRef;
  }

  public void setItemRef(String itemRef) {
    this.itemRef = itemRef;
  }

  public Message clone() {
    Message clone = new Message();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Message otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setItemRef(otherElement.getItemRef());
  }

  /**
   * Creates builder to build {@link Message}.
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder to build {@link Message} and initialize it with the given object.
   * @param message to initialize the builder with
   * @return created builder
   */
  public static Builder builderFrom(Message message) {
    return new Builder(message);
  }

  /**
   * Builder to build {@link Message}.
   */
  public static final class Builder {

    private String id;
    private int xmlRowNumber;
    private int xmlColumnNumber;
    private Map<String, List<ExtensionElement>> extensionElements = emptyMap();
    private Map<String, List<ExtensionAttribute>> attributes = emptyMap();
    private String name;
    private String itemRef;

    private Builder() {
    }

    private Builder(Message message) {
        this.id = message.id;
        this.xmlRowNumber = message.xmlRowNumber;
        this.xmlColumnNumber = message.xmlColumnNumber;
        this.extensionElements = message.extensionElements;
        this.attributes = message.attributes;
        this.name = message.name;
        this.itemRef = message.itemRef;
    }

    public Builder id(String id) {
        this.id = id;
        return this;
    }

    public Builder xmlRowNumber(int xmlRowNumber) {
        this.xmlRowNumber = xmlRowNumber;
        return this;
    }

    public Builder xmlColumnNumber(int xmlColumnNumber) {
        this.xmlColumnNumber = xmlColumnNumber;
        return this;
    }

    public Builder extensionElements(Map<String, List<ExtensionElement>> extensionElements) {
        this.extensionElements = extensionElements;
        return this;
    }

    public Builder attributes(Map<String, List<ExtensionAttribute>> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Builder name(String name) {
        this.name = name;
        return this;
    }

    public Builder itemRef(String itemRef) {
        this.itemRef = itemRef;
        return this;
    }

    public Message build() {
        return new Message(this);
    }
  }
}
