package com.hp.sv.runtime.reports.inmemory.akka.client;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.RoundRobinRouter;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.RuntimeReport;
import com.hp.sv.runtime.reports.inmemory.akka.client.model.RuntimeReportSelector;
import com.hp.sv.runtime.reports.inmemory.akka.client.worker.Worker;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

public class Master extends UntypedActor {

    private static final Log logger = LogFactory.getLog(Master.class);

    private final int workersCount;
    private final ActorRef router;

    public Master(final int workersCount, final ApplicationContext context) {
        Validate.notNull(context);
        Validate.isTrue(workersCount > 0);

        this.workersCount = workersCount;
        router = getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(workersCount)));
    }

    private ActorRef originalSender;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof RuntimeReport) {
            originalSender.forward(message, getContext());
        } else {
            if (message instanceof RuntimeReportSelector) {
                originalSender = getSender();
            }
            router.tell(message, getSelf());
        }
    }

    /*static class WorkerCreator implements Creator<Actor> {
        private ApplicationContext context;

        WorkerCreator(ApplicationContext context) {
            this.context = context;
        }

        @Override
        public Actor create() throws Exception {
            final Worker worker = context.getBean(Worker.class);
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Producing new worker [%s].", worker));
            }
            return worker;
        }
    }    */
}
