package com.hp.sv.simulator.simple.test.integration.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.runtime.reports.api.RuntimeReportsClientException;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class SimulatorTestCase implements AutoCloseable {
    private static final Log logger = LogFactory.getLog(SimulatorTestCase.class);

    private RuntimeReportsClient runtimeReportsClient;
    private Simulator simulator;
    private int virtualServiceId;

    public SimulatorTestCase(RuntimeReportsClient runtimeReportsClient, Simulator simulator, int virtualServiceId) {
        Validate.notNull(runtimeReportsClient);
        Validate.notNull(simulator);

        this.runtimeReportsClient = runtimeReportsClient;
        this.simulator = simulator;
        this.virtualServiceId = virtualServiceId;

        runtimeReportsClient.registerService(virtualServiceId);
    }

    public void execute(final int threadsCount, final int countPerThread) throws Exception {
        Thread[] threads = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < countPerThread; j++) {
                        assertThat(simulator.getResponse(new Object(), virtualServiceId), is(nullValue()));
                    }
                }
            });
        }

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        ExecutorService exec = Executors.newSingleThreadExecutor();
        final Future<Object> future = exec.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                while (runtimeReportsClient.getServiceUsageCount(virtualServiceId) != countPerThread * threadsCount) {
                    Thread.sleep(50);
                }
                return runtimeReportsClient.getServiceUsageCount(virtualServiceId);
            }
        });
        final int result = (int) future.get(100, TimeUnit.SECONDS);
        stopWatch.stop();

        logger.info(String.format("%d simulation calls took: %d ms.", countPerThread * threadsCount, stopWatch.getTime()));
        assertThat(result, is(equalTo(countPerThread * threadsCount)));
    }

    public void close() throws Exception {
        runtimeReportsClient.unregisterService(virtualServiceId);
        try {
            runtimeReportsClient.getServiceUsageCount(virtualServiceId);
            fail();
        } catch (RuntimeReportsClientException e) {
        }
    }
}
