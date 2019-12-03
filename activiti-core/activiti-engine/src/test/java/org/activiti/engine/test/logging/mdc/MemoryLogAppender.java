package org.activiti.engine.test.logging.mdc;

import java.io.StringWriter;

import ch.qos.logback.core.OutputStreamAppender;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * @author Saeid Mirzaei
 */
public class MemoryLogAppender<E> extends OutputStreamAppender<E> {

    StringWriter stringWriter = new StringWriter();

    @Override
    public void start() {
        this.setOutputStream(new WriterOutputStream(stringWriter));
        super.start();
    }

    public String toString() {
        return stringWriter.toString();
    }

    public void clear() {
        stringWriter = new StringWriter();
    }
}
