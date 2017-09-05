package org.activiti.services.audit.mongo.entity;

import java.util.HashMap;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "act_evt_log")
public class EventLogDocument extends HashMap<String, Object> {

    private static final long serialVersionUID = 8576938220345487187L;
}
