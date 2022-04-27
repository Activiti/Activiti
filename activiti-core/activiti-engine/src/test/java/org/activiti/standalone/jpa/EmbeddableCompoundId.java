/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.standalone.jpa;

import java.io.Serializable;

import javax.persistence.Embeddable;


@Embeddable
public class EmbeddableCompoundId implements Serializable {

  private static final long serialVersionUID = 1L;

  private long idPart1;

  private String idPart2;

  public long getIdPart1() {
    return idPart1;
  }

  public void setIdPart1(long idPart1) {
    this.idPart1 = idPart1;
  }

  public String getIdPart2() {
    return idPart2;
  }

  public void setIdPart2(String idPart2) {
    this.idPart2 = idPart2;
  }

  @Override
  public boolean equals(Object obj) {
    EmbeddableCompoundId other = (EmbeddableCompoundId) obj;
    return idPart1 == other.idPart1 && idPart2.equals(other.idPart2);
  }

  @Override
  public int hashCode() {
    return (idPart1 + idPart2).hashCode();
  }

}
