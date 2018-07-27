package org.conf.activiti.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(
        use = NAME,
        include = PROPERTY,
        property = "resultType")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstanceResultMixIn {

}
