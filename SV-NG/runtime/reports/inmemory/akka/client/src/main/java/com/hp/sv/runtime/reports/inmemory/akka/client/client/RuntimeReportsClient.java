package com.hp.sv.runtime.reports.inmemory.akka.client.client;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.hp.sv.runtime.reports.api.RuntimeReportsClientException;
import com.hp.sv.runtime.reports.inmemory.akka.client.Master;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

public class RuntimeReportsClient implements com.hp.sv.runtime.reports.api.RuntimeReportsClient, ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(RuntimeReportsClient.class);

    private ActorSystem actorSystem;
    private ActorRef master;
    private ApplicationContext context;

    @PostConstruct
    public void afterDependenciesInitialization() {
        actorSystem = ActorSystem.create();
        master = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            @Override
            public Actor create() throws Exception {
                return new Master(30, context);
            }
        }), "master");
    }

    @PreDestroy
    public void preDestroy() {
        actorSystem.shutdown();
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
        final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
        Future<Object> f = Patterns.ask(master, rrs, timeout);

        try {
            final Object result = Await.result(f, timeout.duration());
            final int count = ((RuntimeReport) result).getCount();
            return count;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeReportsClientException(String.format("Can't get service usage count for virtual service [Id=%d].", id), e);
        }
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
