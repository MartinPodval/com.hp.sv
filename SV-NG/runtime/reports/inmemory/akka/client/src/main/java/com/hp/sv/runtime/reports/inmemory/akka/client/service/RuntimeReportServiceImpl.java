package com.hp.sv.runtime.reports.inmemory.akka.client.service;

import com.hp.sv.runtime.reports.api.RuntimeReportsService;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeReportServiceImpl implements RuntimeReportsService {
    private static final Log logger = LogFactory.getLog(RuntimeReportServiceImpl.class);

    //    private ConcurrentMap<Integer, AtomicInteger> map = new ConcurrentHashMap<Integer, AtomicInteger>();
    private AtomicInteger i;

    public void registerService(int id) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Registering virtual service [%d].", id));
        }

        Validate.isTrue(id > 0);
//        Validate.isTrue(map.putIfAbsent(id, new AtomicInteger()) == null, "Service [Id=%d] is already registered.", id);
        i = new AtomicInteger();
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Registering virtual service is done [%d].", id));
//            logger.info(String.format("Current value is [%d].", map.get(id).intValue()));
            logger.info(String.format("Current value is [%d].", i.intValue()));
        }
    }

    public void increaseServiceUsageCount(int id) {
//        if (logger.isDebugEnabled()) {
//            logger.debug(String.format("Increasing usage count for virtual service [%d]", id));
//        }

//        Validate.isTrue(id > 0);
//        AtomicInteger count = map.get(id);
        AtomicInteger count = i;
//        Validate.notNull(count);
        final int increasedCount = count.incrementAndGet();

        if (increasedCount % 100000 == 0) {
            logger.info(increasedCount);
        }

//        if (logger.isDebugEnabled()) {
//            logger.debug(String.format("Count for virtual service [Id=%d] was increased to [%d].", id, increasedCount));
//        }
    }

    public Integer getServiceUsageCount(int id) {
//        AtomicInteger count = map.get(id);
        AtomicInteger count = i;
        return count != null ? count.get() : null;
    }

    public void unregisterService(int id) {
//        Validate.notNull(map.remove(id), "Service [Id=%d] is not registered.", id);
        i = null;

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Unregistering virtual service [%d].", id));
        }
    }
}
