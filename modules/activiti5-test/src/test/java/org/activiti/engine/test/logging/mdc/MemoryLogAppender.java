package org.activiti.engine.test.logging.mdc;

import java.io.StringWriter;

import org.apache.log4j.ConsoleAppender;

/**
 * @author Saeid Mirzaei
 */

public class MemoryLogAppender extends ConsoleAppender {

  StringWriter stringWriter = new StringWriter();

  public void activateOptions() {
    setWriter(stringWriter);
  }

  public String toString() {
    return stringWriter.toString();
  }

  public void clear() {
    stringWriter = new StringWriter();
  }

}
