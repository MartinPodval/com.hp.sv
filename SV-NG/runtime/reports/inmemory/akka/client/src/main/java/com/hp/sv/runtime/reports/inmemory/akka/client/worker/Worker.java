package com.hp.sv.runtime.reports.inmemory.akka.client.worker;

import akka.actor.UntypedActor;
import com.hp.sv.runtime.reports.api.RuntimeReportsService;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.*;
import com.hp.sv.runtime.reports.inmemory.akka.client.service.RuntimeReportServiceImpl;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Worker extends UntypedActor {

    private static final Log logger = LogFactory.getLog(Worker.class);

    private static RuntimeReportsService runtimeReportsService = new RuntimeReportServiceImpl();

    @Override
    public void onReceive(Object message) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Message received: %s", message));
        }
        Validate.notNull(runtimeReportsService);

        if (message instanceof VirtualServiceRegistration) {
            runtimeReportsService.registerService(((VirtualServiceRegistration) message).getVirtualServiceId());
        } else if (message instanceof VirtualServiceIncrementation) {
            runtimeReportsService.increaseServiceUsageCount(((VirtualServiceIncrementation) message).getVirtualServiceId());
        } else if (message instanceof VirtualServiceUnregistration) {
            runtimeReportsService.unregisterService(((VirtualServiceUnregistration) message).getVirtualServiceId());
        } else if (message instanceof RuntimeReportSelector) {
            final RuntimeReportSelector selector = (RuntimeReportSelector) message;
            final Integer count = runtimeReportsService.getServiceUsageCount(selector.getVirtualServiceId());
            getSender().tell(new RuntimeReport(selector.getVirtualServiceId(), count), getSelf());
        } else {
            unhandled(message);
        }
    }
}
