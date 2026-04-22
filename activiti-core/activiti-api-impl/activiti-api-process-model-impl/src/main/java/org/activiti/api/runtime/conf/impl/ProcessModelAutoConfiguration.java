/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import tools.jackson.core.Version;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleAbstractTypeResolver;
import tools.jackson.databind.module.SimpleModule;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.BPMNError;
import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.BPMNSequenceFlow;
import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.IntegrationContext;
import org.activiti.api.process.model.MessageSubscription;
import org.activiti.api.process.model.ProcessCandidateStarterGroup;
import org.activiti.api.process.model.ProcessCandidateStarterUser;
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
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterGroupImpl;
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterUserImpl;
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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.FormattingConversionService;
import tools.jackson.databind.JacksonModule;

@AutoConfiguration
@AutoConfigureBefore({ JacksonAutoConfiguration.class })
public class ProcessModelAutoConfiguration {

    @Autowired(required = false)
    @ProcessVariableTypeConverter
    @Lazy
    private Set<Converter<?, ?>> converters = Collections.emptySet();

    //this bean will be automatically injected inside boot's JsonMapper
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public JacksonModule customizeProcessModelObjectMapper(ObjectProvider<ConversionService> conversionServiceProvider) {
        SimpleModule module = new SimpleModule("mapProcessModelInterfaces", Version.unknownVersion());
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();

        resolver.addMapping(BPMNActivity.class, BPMNActivityImpl.class);
        resolver.addMapping(ProcessInstance.class, ProcessInstanceImpl.class);
        resolver.addMapping(ProcessDefinition.class, ProcessDefinitionImpl.class);
        resolver.addMapping(BPMNSequenceFlow.class, BPMNSequenceFlowImpl.class);
        resolver.addMapping(IntegrationContext.class, IntegrationContextImpl.class);
        resolver.addMapping(BPMNSignal.class, BPMNSignalImpl.class);
        resolver.addMapping(BPMNTimer.class, BPMNTimerImpl.class);
        resolver.addMapping(BPMNMessage.class, BPMNMessageImpl.class);
        resolver.addMapping(BPMNError.class, BPMNErrorImpl.class);
        resolver.addMapping(MessageSubscription.class, MessageSubscriptionImpl.class);
        resolver.addMapping(StartMessageSubscription.class, StartMessageSubscriptionImpl.class);
        resolver.addMapping(StartMessageDeployedEvent.class, StartMessageDeployedEventImpl.class);
        resolver.addMapping(StartMessageDeploymentDefinition.class, StartMessageDeploymentDefinitionImpl.class);
        resolver.addMapping(ProcessCandidateStarterUser.class, ProcessCandidateStarterUserImpl.class);
        resolver.addMapping(ProcessCandidateStarterGroup.class, ProcessCandidateStarterGroupImpl.class);

        module.registerSubtypes(
            new NamedType(ProcessInstanceResult.class, ProcessInstanceResult.class.getSimpleName())
        );

        module.registerSubtypes(new NamedType(DeleteProcessPayload.class, DeleteProcessPayload.class.getSimpleName()));
        module.registerSubtypes(
            new NamedType(GetProcessDefinitionsPayload.class, GetProcessDefinitionsPayload.class.getSimpleName())
        );
        module.registerSubtypes(
            new NamedType(GetProcessInstancesPayload.class, GetProcessInstancesPayload.class.getSimpleName())
        );
        module.registerSubtypes(new NamedType(GetVariablesPayload.class, GetVariablesPayload.class.getSimpleName()));
        module.registerSubtypes(
            new NamedType(RemoveProcessVariablesPayload.class, RemoveProcessVariablesPayload.class.getSimpleName())
        );
        module.registerSubtypes(
            new NamedType(SetProcessVariablesPayload.class, SetProcessVariablesPayload.class.getSimpleName())
        );
        module.registerSubtypes(new NamedType(SignalPayload.class, SignalPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(TimerPayload.class, TimerPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(StartProcessPayload.class, StartProcessPayload.class.getSimpleName()));
        module.registerSubtypes(
            new NamedType(CreateProcessInstancePayload.class, CreateProcessInstancePayload.class.getSimpleName())
        );
        module.registerSubtypes(
            new NamedType(SuspendProcessPayload.class, SuspendProcessPayload.class.getSimpleName())
        );
        module.registerSubtypes(new NamedType(ResumeProcessPayload.class, ResumeProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(UpdateProcessPayload.class, UpdateProcessPayload.class.getSimpleName()));
        module.registerSubtypes(new NamedType(StartMessagePayload.class, StartMessagePayload.class.getSimpleName()));
        module.registerSubtypes(
            new NamedType(ReceiveMessagePayload.class, ReceiveMessagePayload.class.getSimpleName())
        );
        module.registerSubtypes(new NamedType(MessageEventPayload.class, MessageEventPayload.class.getSimpleName()));
        module.setAbstractTypes(resolver);

        Supplier<ConversionService> conversionServiceSupplier = () -> Objects.requireNonNullElse(conversionServiceProvider.getIfUnique(), this.conversionService());
        module.addSerializer(new ProcessVariablesMapSerializer(conversionServiceSupplier));

        module.addDeserializer(ProcessVariablesMap.class, new ProcessVariablesMapDeserializer(conversionServiceSupplier));

        return module;
    }

    public FormattingConversionService conversionService() {
        ApplicationConversionService conversionService = new ApplicationConversionService();

        converters.forEach(conversionService::addConverter);

        return conversionService;
    }

    @Bean
    public StringToMapConverter stringToMapConverter(@Lazy JsonMapper jsonMapper) {
        return new StringToMapConverter(jsonMapper);
    }

    @Bean
    public MapToStringConverter mapToStringConverter(@Lazy JsonMapper jsonMapper) {
        return new MapToStringConverter(jsonMapper);
    }

    @Bean
    public StringToJsonNodeConverter stringToJsonNodeConverter(@Lazy JsonMapper jsonMapper) {
        return new StringToJsonNodeConverter(jsonMapper);
    }

    @Bean
    public JsonNodeToStringConverter jsonNodeToStringConverter(@Lazy JsonMapper jsonMapper) {
        return new JsonNodeToStringConverter(jsonMapper);
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
    public StringToListConverter sringToListConverter(@Lazy JsonMapper jsonMapper) {
        return new StringToListConverter(jsonMapper);
    }

    @Bean
    public ListToStringConverter listToStringConverter(@Lazy JsonMapper jsonMapper) {
        return new ListToStringConverter(jsonMapper);
    }

    @Bean
    public StringToSetConverter stringToSetConverter(@Lazy JsonMapper jsonMapper) {
        return new StringToSetConverter(jsonMapper);
    }

    @Bean
    public SetToStringConverter setToStringConverter(@Lazy JsonMapper jsonMapper) {
        return new SetToStringConverter(jsonMapper);
    }

    @Bean
    public StringToObjectValueConverter stringToObjectValueConverter(@Lazy JsonMapper jsonMapper) {
        return new StringToObjectValueConverter(jsonMapper);
    }

    @Bean
    public ObjectValueToStringConverter objectValueToStringConverter(@Lazy JsonMapper jsonMapper) {
        return new ObjectValueToStringConverter(jsonMapper);
    }
}
