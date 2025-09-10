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

package org.activiti.engine.impl.cfg.configuration;

import java.util.List;

import org.activiti.core.el.CustomFunctionProvider;
import org.activiti.engine.impl.agenda.DefaultActivitiEngineAgendaFactory;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.util.DefaultClockImpl;

/**
 * Core configuration for basic engine components like expression manager, 
 * agenda factory, clock, and business calendar manager.
 */
public class CoreConfiguration {

    private final ProcessEngineConfigurationImpl config;

    public CoreConfiguration(ProcessEngineConfigurationImpl config) {
        this.config = config;
    }

    /**
     * Initialize all core components.
     */
    public void configure() {
        initHistoryLevel();
        initExpressionManager();
        initAgendaFactory();
        initClock();
        initBusinessCalendarManager();
    }

    public void initHistoryLevel() {
        if (config.getHistoryLevel() == null) {
            config.setHistoryLevel(HistoryLevel.getHistoryLevelForKey(config.getHistory()));
        }
    }

    public void initExpressionManager() {
        if (config.getExpressionManager() == null) {
            ExpressionManager expressionManager = new ExpressionManager(config.getBeans());
            List<CustomFunctionProvider> customFunctionProviders = config.getCustomFunctionProviders();
            if (customFunctionProviders != null) {
                expressionManager.setCustomFunctionProviders(customFunctionProviders);
            }
            config.setExpressionManager(expressionManager);
        }
    }

    public void initAgendaFactory() {
        if (config.getEngineAgendaFactory() == null) {
            config.setEngineAgendaFactory(new DefaultActivitiEngineAgendaFactory());
        }
    }

    public void initClock() {
        if (config.getClock() == null) {
            config.setClock(new DefaultClockImpl());
        }
    }

    public void initBusinessCalendarManager() {
        if (config.getBusinessCalendarManager() == null) {
            MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
            mapBusinessCalendarManager.addBusinessCalendar(
                DurationBusinessCalendar.NAME,
                new DurationBusinessCalendar(config.getClock())
            );
            mapBusinessCalendarManager.addBusinessCalendar(
                DueDateBusinessCalendar.NAME,
                new DueDateBusinessCalendar(config.getClock())
            );
            mapBusinessCalendarManager.addBusinessCalendar(
                CycleBusinessCalendar.NAME,
                new CycleBusinessCalendar(config.getClock())
            );

            config.setBusinessCalendarManager(mapBusinessCalendarManager);
        }
    }
}