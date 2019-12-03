package org.activiti.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

public abstract class AbstractActivitiSmartLifeCycle implements SmartLifecycle, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(AbstractActivitiSmartLifeCycle.class);
    
    private Object lifeCycleMonitor = new Object();
    private boolean autoStartup = true;
    private int phase = DEFAULT_PHASE;
    private volatile boolean running = false;
    
    public AbstractActivitiSmartLifeCycle() {
    }
    
    public abstract void doStart();

    public abstract void doStop();
    
    /**
     * Set whether to auto-start the activation after this component
     * has been initialized and the context has been refreshed.
     * <p>Default is "true". Turn this flag off to defer the endpoint
     * activation until an explicit {@link #start()} call.
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /**
     * Return the value for the 'autoStartup' property. If "true", this
     * component will start upon a ContextRefreshedEvent.
     */
    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    /**
     * Specify the phase in which this component should be started
     * and stopped. The startup order proceeds from lowest to highest, and
     * the shutdown order is the reverse of that. By default this value is
     * Integer.MAX_VALUE meaning that this component starts as late
     * as possible and stops as soon as possible.
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Return the phase in which this component will be started and stopped.
     */
    @Override
    public int getPhase() {
        return this.phase;
    }
    
    @Override
    public void start() {
        synchronized (this.lifeCycleMonitor) {
            if (!this.running) {
                logger.info("Starting...");
                
                doStart();
                
                this.running = true;
                logger.info("Started.");
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this.lifeCycleMonitor) {
            if (this.running) {
                logger.info("Stopping...");
                doStop();
                
                this.running = false;
                logger.info("Stopped.");
            }
        }
    }

    @Override
    public void stop(Runnable callback) {
        synchronized (this.lifeCycleMonitor) {
            stop();
            callback.run();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void destroy() {
        stop();
    }
    
}
