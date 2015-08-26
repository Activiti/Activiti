/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import javax.inject.Inject;
import java.util.Properties;

@Configuration
public class EmailConfiguration {

    @Inject
    private Environment env;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        
        boolean isEmailEnabled = env.getProperty("email.enabled", Boolean.class, false);
        if (isEmailEnabled) {
            sender.setHost(env.getRequiredProperty("email.host"));
            sender.setPort(env.getRequiredProperty("email.port", Integer.class));
        } 
        
        Boolean useCredentials = env.getProperty("email.useCredentials", Boolean.class);
        if (Boolean.TRUE.equals(useCredentials)) {
            sender.setUsername(env.getProperty("email.username"));
            sender.setPassword(env.getProperty("email.password"));
        }
        
        return sender;
    }
    
    @Bean
    public FreeMarkerConfigurationFactoryBean freeMarkerConfig() {
        Properties props = new Properties();
        props.setProperty("number_format","0.##");
        props.setProperty("locale","en-US");

        FreeMarkerConfigurationFactoryBean factory = new FreeMarkerConfigurationFactoryBean();
        factory.setFreemarkerSettings(props);
        factory.setTemplateLoaderPath("classpath:/email-templates/");
        return factory;
    }
}
