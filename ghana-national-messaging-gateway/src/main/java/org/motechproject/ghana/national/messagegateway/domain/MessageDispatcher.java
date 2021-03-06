package org.motechproject.ghana.national.messagegateway.domain;

import org.motechproject.ghana.national.messagegateway.service.MessageGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ReleaseStrategy;

import java.util.List;

@MessageEndpoint
public class MessageDispatcher {

    public static final String SMS_SEPARATOR = "%0a";

    @Autowired
    private MessageGateway messageGateway;

    @Autowired
    AggregationStrategy aggregationStrategy;

    public List<Payload> aggregate(List<Payload> payloads) {
        return aggregationStrategy.aggregate(payloads);
    }

    @CorrelationStrategy
    public String correlateByRecipientAndDeliveryDate(Payload payload) {
        return new PayloadIdentifier(payload).toString();
    }

    @ReleaseStrategy
    public boolean canBeDispatched(List<Payload> payloads) {
        return payloads.get(0).canBeDispatched();
    }
}
