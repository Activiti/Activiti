/*
 * Copyright 2018 Diffblue Limited
 *
 * Diffblue Limited licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.impl.util.json;

import org.activiti.engine.impl.util.json.XML;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class XMLTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  /* testedClasses: XML.java */
  /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - org/activiti/engine/impl/util/json/XML.java:80: loop: 1 iterations
   *  - iteration 1
   *     - conditional line 81 branch to line 81
   */

  @Test
  public void escapeInputNotNullOutputNotNull2() {

    // Arrange
    final String string = "!";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("!", retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - org/activiti/engine/impl/util/json/XML.java:80: loop: 1 iterations
   *  - iteration 1
   *     - conditional line 81 branch to line 81
   *     - case 3 of switch on line 82
   */

  @Test
  public void escapeInputNotNullOutputNotNull3() {

    // Arrange
    final String string = "<";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("&lt;", retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - org/activiti/engine/impl/util/json/XML.java:80: loop: 1 iterations
   *  - iteration 1
   *     - conditional line 81 branch to line 81
   *     - case 1 of switch on line 82
   */

  @Test
  public void escapeInputNotNullOutputNotNull4() {

    // Arrange
    final String string = "\"";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("&quot;", retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - org/activiti/engine/impl/util/json/XML.java:80: loop: 1 iterations
   *  - iteration 1
   *     - conditional line 81 branch to line 81
   *     - case 4 of switch on line 82
   */

  @Test
  public void escapeInputNotNullOutputNotNull6() {

    // Arrange
    final String string = ">";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("&gt;", retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - org/activiti/engine/impl/util/json/XML.java:80: loop: 1 iterations
   *  - iteration 1
   *     - conditional line 81 branch to line 81
   *     - case 2 of switch on line 82
   */

  @Test
  public void escapeInputNotNullOutputNotNull5() {

    // Arrange
    final String string = "&";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("&amp;", retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *
   */

  @Test
  public void escapeInputNotNullOutputNotNull() {

    // Arrange
    final String string = "";

    // Act
    final String retval = XML.escape(string);

    // Assert result
    Assert.assertEquals("", retval);
  }
}
