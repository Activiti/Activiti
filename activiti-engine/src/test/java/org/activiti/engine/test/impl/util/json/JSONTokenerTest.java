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

import org.activiti.engine.impl.util.json.JSONTokener;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JSONTokenerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  /* testedClasses: JSONTokener.java */
  /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 95
   *  - conditional line 95 branch to line 96
   */

  @Test
  public void dehexcharInput0OutputZero() {

    // Arrange
    final char c = '0';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(0, retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 95
   *  - conditional line 95 branch to line 98
   *  - conditional line 98 branch to line 98
   *  - conditional line 98 branch to line 101
   *  - conditional line 101 branch to line 101
   *  - conditional line 101 branch to line 102
   */

  @Test
  public void dehexcharInputfOutputPositive() {

    // Arrange
    final char c = 'f';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(15, retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 95
   *  - conditional line 95 branch to line 98
   *  - conditional line 98 branch to line 98
   *  - conditional line 98 branch to line 99
   */

  @Test
  public void dehexcharInputFOutputPositive() {

    // Arrange
    final char c = 'F';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(15, retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 95
   *  - conditional line 95 branch to line 98
   *  - conditional line 98 branch to line 98
   *  - conditional line 98 branch to line 101
   *  - conditional line 101 branch to line 104
   */

  @Test
  public void dehexcharInputGOutputNegative() {

    // Arrange
    final char c = 'G';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(-1, retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 98
   *  - conditional line 98 branch to line 101
   *  - conditional line 101 branch to line 104
   */

  @Test
  public void dehexcharInputNotNullOutputNegative() {

    // Arrange
    final char c = ' ';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(-1, retval);
  }

    /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   *  - conditional line 95 branch to line 95
   *  - conditional line 95 branch to line 98
   *  - conditional line 98 branch to line 98
   *  - conditional line 98 branch to line 101
   *  - conditional line 101 branch to line 101
   *  - conditional line 101 branch to line 104
   */

  @Test
  public void dehexcharInputoOutputNegative() {

    // Arrange
    final char c = 'o';

    // Act
    final int retval = JSONTokener.dehexchar(c);

    // Assert result
    Assert.assertEquals(-1, retval);
  }
}
