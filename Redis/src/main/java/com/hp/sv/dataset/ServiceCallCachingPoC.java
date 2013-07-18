package com.hp.sv.dataset;

import com.esotericsoftware.kryo.Kryo;
import com.hp.sv.Repository;
import com.hp.sv.dataset.model.Leaf;
import com.hp.sv.dataset.model.LeafCollection;
import com.hp.sv.dataset.model.ServiceCall;
import com.hp.sv.dataset.model.ServiceCallFactory;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class ServiceCallCachingPoC {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(ServiceCallCachingPoC.class.getName());
    private final Repository repository;
    private final ServiceCallFactory serviceCallFactory;

    public ServiceCallCachingPoC(String connectionString) {
        repository = new Repository(connectionString);
        serviceCallFactory = new ServiceCallFactory(repository);
    }

    public void Dispose() {
        repository.Dispose();
    }

    public void Test() {
        final int callsCount = 200000;
        final int leavesCount = 400;

        Iterable<ServiceCall> serviceCalls = serviceCallFactory.GenerateCalls(callsCount, leavesCount);
        logger.info("All service calls created.");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Kryo kryo = new Kryo();
//        kryo.register(ServiceCall.class, 0);
//        kryo.register(LeafCollection.class, 1);
//        kryo.register(Leaf.class, 2);

        try {
            List<Long> ids = new ArrayList<>();
            stopWatch.suspend();
            for (ServiceCall serviceCall : serviceCalls) {
                stopWatch.resume();
//                repository.AddServiceCallAsKeyValuePairs(serviceCall);
//                repository.AddServiceCallAsExternalizableByteArray(serviceCall);
                repository.AddServiceCallAsKryoByteArray(serviceCall, kryo);
                stopWatch.suspend();
                ids.add(serviceCall.getId());
            }

            stopWatch.stop();
            logger.info("Total write time [ms]: " + stopWatch.getTime());
            logger.info("Service calls: {}, leaves per each: {}.", callsCount, leavesCount);

            stopWatch.reset();
            stopWatch.start();

            for (Long id : ids) {
//                ServiceCall fetchedServiceCall = repository.GetServiceCallAsKeyValuePairs(id);
//                ServiceCall fetchedServiceCall = repository.GetServiceCallAsExternalizableByteArray(id);
                ServiceCall fetchedServiceCall = repository.GetServiceCallAsKryoByteArray(id, kryo);
                Validate.notNull(fetchedServiceCall);
                Validate.isTrue(fetchedServiceCall.getId().equals(id), "Required id=%d but %d was supplied", fetchedServiceCall.getId(), id);
                Validate.isTrue(fetchedServiceCall.GetLeavesCount() == leavesCount);
            }

        } catch (Exception e) {
            logger.error("Test failed.", e);
        }

        stopWatch.stop();
        logger.info("Total read time [ms]: " + stopWatch.getTime());
    }
}
