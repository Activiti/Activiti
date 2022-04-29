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


package org.activiti.engine.impl.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractTestCase extends TestCase {

  protected static final String EMPTY_LINE = "\n";

  protected static Logger log = LoggerFactory.getLogger(AbstractTestCase.class);

  protected boolean isEmptyLinesEnabled = true;

  /**
   * Asserts if the provided text is part of some text.
   */
  public void assertTextPresent(String expected, String actual) {
    if ((actual == null) || (!actual.contains(expected))) {
      throw new AssertionFailedError("expected presence of [" + expected + "], but was [" + actual + "]");
    }
  }

  /**
   * Asserts if the provided text is part of some text, ignoring any uppercase characters
   */
  public void assertTextPresentIgnoreCase(String expected, String actual) {
    assertTextPresent(expected.toLowerCase(), actual.toLowerCase());
  }

  @Override
  protected void runTest() throws Throwable {
    if (log.isDebugEnabled()) {
      if (isEmptyLinesEnabled) {
        log.debug(EMPTY_LINE);
      }
      log.debug("#### START {}.{} ###########################################################", this.getClass().getSimpleName(), getName());
    }

    try {

      super.runTest();

    } catch (AssertionFailedError e) {
      log.error(EMPTY_LINE);
      log.error("ASSERTION FAILED: {}", e, e);
      throw e;

    } catch (Throwable e) {
      log.error(EMPTY_LINE);
      log.error("EXCEPTION: {}", e, e);
      throw e;

    } finally {
      log.debug("#### END {}.{} #############################################################", this.getClass().getSimpleName(), getName());
    }
  }

}
