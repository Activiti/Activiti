package org.activiti.services.audit.producer.app;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * Created by msalatino on 23/06/2017.
 */
public interface AuditProducerChannels {
    String AUDIT_PRODUCER = "auditProducer";
    @Output(AUDIT_PRODUCER)
    MessageChannel auditProducer();


}
