package com.hp.sv.simulator.simple.test.integration.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.runtime.reports.api.RuntimeReportsClientException;
import com.hp.sv.runtime.reports.inmemory.rest.service.RuntimeReportRestfulServiceImpl;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulatorImplTestWithJerseyAndEmbeddedGrizzly extends JerseyTest {

    private static final Log logger = LogFactory.getLog(SimulatorImplTestWithJerseyAndEmbeddedGrizzly.class);

    @Autowired
    protected Simulator simulator;

    @Autowired
    @Qualifier("runtimeReportClient")
    protected RuntimeReportsClient runtimeReportsClient;

    private int virtualServiceId = 123;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        runtimeReportsClient.registerService(virtualServiceId);
    }

    @After
    public void tearDown() throws Exception {
        runtimeReportsClient.unregisterService(virtualServiceId);
        try {
            runtimeReportsClient.getServiceUsageCount(virtualServiceId);
            fail();
        } catch (RuntimeReportsClientException e) {
        }
        super.tearDown();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = new ResourceConfig()
                .register(SpringLifecycleListener.class)
                .register(RequestContextFilter.class);
        rc.property("contextConfigLocation", "classpath*:/spring/config.xml");
        rc.register(RuntimeReportRestfulServiceImpl.class);
        return rc;
    }

    @Test
    public void getResponse_returns_response() throws InterruptedException {
        final int countPerThread = 5000;
        final int threadsCount = 10;
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

        stopWatch.stop();
        logger.info(String.format("%d simulation calls took: %d ms.", countPerThread * threadsCount, stopWatch.getTime()));

        assertThat(runtimeReportsClient.getServiceUsageCount(virtualServiceId), is(equalTo(countPerThread * threadsCount)));
    }
}
