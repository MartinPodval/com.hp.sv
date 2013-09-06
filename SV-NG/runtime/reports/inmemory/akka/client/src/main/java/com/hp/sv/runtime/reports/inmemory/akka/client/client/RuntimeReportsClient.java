package com.hp.sv.runtime.reports.inmemory.akka.client.client;

import akka.actor.*;
import com.hp.sv.runtime.reports.inmemory.akka.client.Master;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.RuntimeReportSelector;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.VirtualServiceIncrementation;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.VirtualServiceRegistration;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.VirtualServiceUnregistration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;

public class RuntimeReportsClient implements com.hp.sv.runtime.reports.api.RuntimeReportsClient, ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(RuntimeReportsClient.class);

    private ActorRef master;
    private ApplicationContext context;

    @PostConstruct
    public void afterDependenciesInitialization() {
        final ActorSystem actorSystem = ActorSystem.create();
        master = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new Master(30, context);
            }
        }), "master");
    }

    private VirtualServiceRegistration reg;

    @Override
    public void registerService(int id) {
        if (reg == null) {
            reg = new VirtualServiceRegistration(id);
        }

        master.tell(reg, ActorRef.noSender());
    }

    private VirtualServiceIncrementation inc;

    @Override
    public void increaseServiceUsageCount(int id) {
        if (inc == null) {
            inc = new VirtualServiceIncrementation(id);
        }
        master.tell(inc, ActorRef.noSender());
    }

    private RuntimeReportSelector rrs;

    @Override
    public int getServiceUsageCount(int id) {
        if (rrs == null) {
            rrs = new RuntimeReportSelector(id);
        }
        master.tell(rrs, ActorRef.noSender());
        return 0;
    }

    @Override
    public void unregisterService(int id) {
        master.tell(new VirtualServiceUnregistration(id), ActorRef.noSender());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
