/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.editor.language.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.converter.util.CommaSplitter;
import org.junit.jupiter.api.Test;



public class CommaSplitterTest {

  @Test
  public void testNoComma() {
    String testString = "Test String";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testString);
  }

  @Test
  public void testOneComa() {
    String testString = "Test,String";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("Test");
    assertThat(result.get(1)).isEqualTo("String");
  }

  @Test
  public void testManyCommas() {
    String testString = "does,anybody,realy,reads,this,nonsense";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(6);
    assertThat(result.get(0)).isEqualTo("does");
    assertThat(result.get(1)).isEqualTo("anybody");
    assertThat(result.get(2)).isEqualTo("realy");
    assertThat(result.get(3)).isEqualTo("reads");
    assertThat(result.get(4)).isEqualTo("this");
    assertThat(result.get(5)).isEqualTo("nonsense");
  }

  @Test
  public void testCommaAtStart() {
    String testString = ",first,second";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("first");
    assertThat(result.get(1)).isEqualTo("second");

  }

  @Test
  public void testCommaAtEnd() {
    String testString = "first,second,";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("first");
    assertThat(result.get(1)).isEqualTo("second");

  }

  @Test
  public void testCommaAtStartAndEnd() {
    String testString = ",first,second,";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("first");
    assertThat(result.get(1)).isEqualTo("second");
  }

  @Test
  public void testOneComaInExpression() {
    String testString = "${first,second}";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testString);
  }

  @Test
  public void testOManyComaInExpression() {
    String testString = "${Everything,should,be,made,as,simple,as,possible},but,no,simpler";
    List<String> result = CommaSplitter.splitCommas(testString);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(4);
    assertThat(result.get(0)).isEqualTo("${Everything,should,be,made,as,simple,as,possible}");
    assertThat(result.get(1)).isEqualTo("but");
    assertThat(result.get(2)).isEqualTo("no");
    assertThat(result.get(3)).isEqualTo("simpler");
  }
}
