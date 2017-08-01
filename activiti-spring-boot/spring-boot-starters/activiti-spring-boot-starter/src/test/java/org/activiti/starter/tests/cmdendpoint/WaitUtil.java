package org.activiti.starter.tests.cmdendpoint;

import java.util.concurrent.atomic.AtomicBoolean;

public class WaitUtil {

    public static void waitFor(AtomicBoolean check,
                               long timeout) throws InterruptedException {
        long elapsed = 0;
        long waitFor = 100;
        while (!check.get()) {
            if(elapsed < timeout) {
                System.out.println("Waiting for " + check + " to be true or " + elapsed + " timeout at: " + timeout + " ...");
                Thread.sleep(waitFor);
                elapsed += waitFor;
            }else{
                throw new IllegalStateException("Operation Time Out");
            }
        }
    }
}
