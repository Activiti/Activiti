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
package org.activiti.api.runtime.conf.impl;

import java.util.Collections;
import java.util.Set;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.BPMNError;
import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.BPMNSequenceFlow;
import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.model.MessageSubscription;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.StartMessageDeploymentDefinition;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.model.payloads.CreateProcessInstancePayload;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.process.model.payloads.GetProcessInstancesPayload;
import org.activiti.api.process.model.payloads.GetVariablesPayload;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.RemoveProcessVariablesPayload;
import org.activiti.api.process.model.payloads.ResumeProcessPayload;
import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.payloads.SuspendProcessPayload;
import org.activiti.api.process.model.payloads.TimerPayload;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.process.model.results.ProcessInstanceResult;
import org.activiti.api.runtime.event.impl.StartMessageDeployedEventImpl;
import org.activiti.api.runtime.model.impl.BPMNActivityImpl;
import org.activiti.api.runtime.model.impl.BPMNErrorImpl;
import org.activiti.api.runtime.model.impl.BPMNMessageImpl;
import org.activiti.api.runtime.model.impl.BPMNSequenceFlowImpl;
import org.activiti.api.runtime.model.impl.BPMNSignalImpl;
import org.activiti.api.runtime.model.impl.BPMNTimerImpl;
import org.activiti.api.runtime.model.impl.DateToStringConverter;
import org.activiti.api.runtime.model.impl.IntegrationContextImpl;
import org.activiti.api.runtime.model.impl.JsonNodeToStringConverter;
import org.activiti.api.runtime.model.impl.ListToStringConverter;
import org.activiti.api.runtime.model.impl.LocalDateTimeToStringConverter;
import org.activiti.api.runtime.model.impl.LocalDateToStringConverter;
import org.activiti.api.runtime.model.impl.MapToStringConverter;
import org.activiti.api.runtime.model.impl.MessageSubscriptionImpl;
import org.activiti.api.runtime.model.impl.ObjectValueToStringConverter;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.model.impl.ProcessVariableTypeConverter;
import org.activiti.api.runtime.model.impl.ProcessVariablesMap;
import org.activiti.api.runtime.model.impl.ProcessVariablesMapDeserializer;
import org.activiti.api.runtime.model.impl.ProcessVariablesMapSerializer;
import org.activiti.api.runtime.model.impl.SetToStringConverter;
import org.activiti.api.runtime.model.impl.StartMessageDeploymentDefinitionImpl;
import org.activiti.api.runtime.model.impl.StartMessageSubscriptionImpl;
import org.activiti.api.runtime.model.impl.StringToDateConverter;
import org.activiti.api.runtime.model.impl.StringToJsonNodeConverter;
import org.activiti.api.runtime.model.impl.StringToListConverter;
import org.activiti.api.runtime.model.impl.StringToLocalDateConverter;
import org.activiti.api.runtime.model.impl.StringToLocalDateTimeConverter;
import org.activiti.api.runtime.model.impl.StringToMapConverter;
import org.activiti.api.runtime.model.impl.StringToObjectValueConverter;
import org.activiti.api.runtime.model.impl.StringToSetConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.FormattingConversionService;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

@AutoConfigureBefore({JacksonAutoConfiguration.class})
@Configuration
public class ProcessModelAutoConfiguration {

    @Autowired(required = false)
    @ProcessVariableTypeConverter
    private Set<Converter<?, ?>> converters = Collections.emptySet();


    //this bean will be automatically injected inside boot's ObjectMapper
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Module customizeProcessModelObjectMapper(ObjectProvider<ConversionService> conversionServiceProvider) {
        SimpleModule module = new SimpleModule("mapProcessModelInterfaces",
                                               Version.unknownVersion());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver() {
            //this is a workaround for https://github.com/FasterXML/jackson-databind/issues/2019
            //once version 2.9.6 is related we can remove this @override method
            @Override
            public JavaType resolveAbstractType(DeserializationConfig config,
                                                BeanDescription typeDesc) {
                return findTypeMapping(config,
                                       typeDesc.getType());
            }
        };

        resolver.addMapping(BPMNActivity.class,
                            BPMNActivityImpl.class);
        resolver.addMapping(ProcessInstance.class,
                            ProcessInstanceImpl.class);
        resolver.addMapping(ProcessDefinition.class,
                            ProcessDefinitionImpl.class);
        resolver.addMapping(BPMNSequenceFlow.class,
                            BPMNSequenceFlowImpl.class);
        resolver.addMapping(IntegrationContext.class,
                            IntegrationContextImpl.class);
        resolver.addMapping(BPMNSignal.class,
        					BPMNSignalImpl.class);
        resolver.addMapping(BPMNTimer.class,
                            BPMNTimerImpl.class);
        resolver.addMapping(BPMNMessage.class,
                            BPMNMessageImpl.class);
        resolver.addMapping(BPMNError.class,
                            BPMNErrorImpl.class);
        resolver.addMapping(MessageSubscription.class,
                            MessageSubscriptionImpl.class);
        resolver.addMapping(StartMessageSubscription.class,
                            StartMessageSubscriptionImpl.class);
        resolver.addMapping(StartMessageDeployedEvent.class,
                            StartMessageDeployedEventImpl.class);
        resolver.addMapping(StartMessageDeploymentDefinition.class,
                            StartMessageDeploymentDefinitionImpl.class);

        module.registerSubtypes(new NamedType(ProcessInstanceResult.class,
                                              ProcessInstanceResult.class.getSimpleName()));

        module.registerSubtypes(new NamedType(DeleteProcessPayload.class,
                                              DeleteProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetProcessDefinitionsPayload.class,
                                              GetProcessDefinitionsPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetProcessInstancesPayload.class,
                                              GetProcessInstancesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(GetVariablesPayload.class,
                                              GetVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(RemoveProcessVariablesPayload.class,
                                              RemoveProcessVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SetProcessVariablesPayload.class,
                                              SetProcessVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SignalPayload.class,
                                              SignalPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(TimerPayload.class,
                                              TimerPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(StartProcessPayload.class,
                                              StartProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(CreateProcessInstancePayload.class,
                                              CreateProcessInstancePayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(SuspendProcessPayload.class,
                                              SuspendProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(ResumeProcessPayload.class,
                                              ResumeProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(UpdateProcessPayload.class,
                                              UpdateProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(StartMessagePayload.class,
                                              StartMessagePayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(ReceiveMessagePayload.class,
                                              ReceiveMessagePayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(MessageEventPayload.class,
                                              MessageEventPayload.class.getSimpleName()));
        module.setAbstractTypes(resolver);

        ConversionService conversionService = conversionServiceProvider.getIfUnique(this::conversionService);

        module.addSerializer(new ProcessVariablesMapSerializer(conversionService));

        module.addDeserializer(ProcessVariablesMap.class,
                               new ProcessVariablesMapDeserializer(conversionService));

        return module;
    }

    public FormattingConversionService conversionService() {
        ApplicationConversionService conversionService = new ApplicationConversionService();

        converters.forEach(conversionService::addConverter);

        return conversionService;
    }

    @Bean
    public StringToMapConverter stringToMapConverter(@Lazy ObjectMapper objectMapper) {
        return new StringToMapConverter(objectMapper);
    }

    @Bean
    public MapToStringConverter mapToStringConverter(@Lazy ObjectMapper objectMapper) {
        return new MapToStringConverter(objectMapper);
    }

    @Bean
    public StringToJsonNodeConverter stringToJsonNodeConverter(@Lazy ObjectMapper objectMapper) {
        return new StringToJsonNodeConverter(objectMapper);
    }

    @Bean
    public JsonNodeToStringConverter jsonNodeToStringConverter(@Lazy ObjectMapper objectMapper) {
        return new JsonNodeToStringConverter(objectMapper);
    }

    @Bean
    public StringToDateConverter stringToDateConverter() {
        return new StringToDateConverter();
    }

    @Bean
    public DateToStringConverter dateToStringConverter() {
        return new DateToStringConverter();
    }

    @Bean
    public StringToLocalDateTimeConverter stringToLocalDateTimeConverter() {
        return new StringToLocalDateTimeConverter();
    }

    @Bean
    public LocalDateTimeToStringConverter localDateTimeToStringConverter() {
        return new LocalDateTimeToStringConverter();
    }

    @Bean
    public StringToLocalDateConverter stringToLocalDateConverter() {
        return new StringToLocalDateConverter();
    }

    @Bean
    public LocalDateToStringConverter localDateToStringConverter() {
        return new LocalDateToStringConverter();
    }

    @Bean
    public StringToListConverter sringToListConverter(@Lazy ObjectMapper objectMapper) {
        return new StringToListConverter(objectMapper);
    }

    @Bean
    public ListToStringConverter listToStringConverter(@Lazy ObjectMapper objectMapper) {
        return new ListToStringConverter(objectMapper);
    }

    @Bean
    public StringToSetConverter stringToSetConverter(@Lazy ObjectMapper objectMapper) {
        return new StringToSetConverter(objectMapper);
    }

    @Bean
    public SetToStringConverter setToStringConverter(@Lazy ObjectMapper objectMapper) {
        return new SetToStringConverter(objectMapper);
    }

    @Bean
    public StringToObjectValueConverter stringToObjectValueConverter(@Lazy ObjectMapper objectMapper) {
        return new StringToObjectValueConverter(objectMapper);
    }

    @Bean
    public ObjectValueToStringConverter objectValueToStringConverter(@Lazy ObjectMapper objectMapper) {
        return new ObjectValueToStringConverter(objectMapper);
    }

}
