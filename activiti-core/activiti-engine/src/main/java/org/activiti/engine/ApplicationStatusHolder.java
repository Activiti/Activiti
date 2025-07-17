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
package org.activiti.engine;

/**
 * Associates a given application status with the current application execution.
 * The purpose of the class is to provide a convenient way to mark the application in shutdown in order to apply
 * different strategies depending on it.
 * This status is not set automatically by Activiti, it must be set by the user application depending on other
 * frameworks in use. For instance if we are using Spring Boot we can catch the
 * org.springframework.context.event.ContextClosedEvent and mark the application as in shutdown.
 */
public class ApplicationStatusHolder {

    private static boolean running = true;

    public static void shutdown(){
        ApplicationStatusHolder.running = false;
    }

    public static boolean isShutdownInProgress() {
        return !running;
    }

    public static boolean isRunning() {
        return running;
    }
}
