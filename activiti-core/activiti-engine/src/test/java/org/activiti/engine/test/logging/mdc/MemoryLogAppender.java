package org.activiti.engine.test.logging.mdc;

import java.io.ByteArrayOutputStream;

import ch.qos.logback.core.OutputStreamAppender;

/**
 * @author Saeid Mirzaei
 */
public class MemoryLogAppender<E> extends OutputStreamAppender<E> {

    ByteArrayOutputStream baos;

    @Override
    public void start() {
        this.init();
        super.start();
    }

    private void init() {
        baos = new ByteArrayOutputStream();
        this.setOutputStream(baos);
    }

    public String toString() {
        return new String(baos.toByteArray());
    }

    public void clear() {
        this.init();
    }

}
