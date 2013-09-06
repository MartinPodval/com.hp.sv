package com.hp.sv.runtime.reports.inmemory.akka.client;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.RoundRobinRouter;
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
        router = getContext().actorOf(Props.create(new WorkerCreator(context)).withRouter(new RoundRobinRouter(workersCount)));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        router.tell(message, getSelf());
    }

    static class WorkerCreator implements Creator<Actor> {
        private ApplicationContext context;

        WorkerCreator(ApplicationContext context) {
            this.context = context;
        }

        @Override
        public Actor create() throws Exception {
            final Worker worker = context.getBean(Worker.class);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Producing new worker [%s].", worker));
            }
            return worker;
        }
    }
}