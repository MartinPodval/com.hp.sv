package com.hp.sv.simulator.simple.test.integration.simulator;


import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.runtime.reports.api.RuntimeReportsClientException;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * How to run this test as parametrized mvn task?
 * <code>
 * mvn integration-test -Dtest=SimulatorImplTest -DargLine="-Dserver.port=12345"
 * </code>
 * <p/>
 * or
 * <p/>
 * <code>
 * mvn integration-test -DargLine="-Dserver.url=http:server -Dserver.port=12345"
 * </code>
 * <p/>
 * Run test from command line:
 * <code>
  * java "-DthreadsCount=50" "-Dserver.port=12345" "-DcountPerThread=100" -cp ".;.\dependency\*;.\SimpleSimulatorIntegrationTest-1.0-SNAPSHOT-tests.jar" org.junit.runner.JUnitCore com.hp.sv.simulator.simple.test.integration.simulator.SimulatorImplTest
 * * </code>
 */
@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulatorImplTest {

    private static final Log logger = LogFactory.getLog(SimulatorImplTest.class);

    @Autowired
    protected Simulator simulator;

    @Autowired
    @Qualifier("runtimeReportClient")
    protected RuntimeReportsClient runtimeReportsClient;

    protected static int threadsCount = 15;
    protected static int countPerThread = 5000;

    private int virtualServiceId = Math.abs(new Random().nextInt());

    @Before
    public void setUp() throws Exception {
        runtimeReportsClient.registerService(virtualServiceId);
        final String threadsCountString = System.getProperty("threadsCount");
        if (threadsCountString != null) {
            threadsCount = Integer.parseInt(threadsCountString);
        }
        final String countPerThreadString = System.getProperty("countPerThread");
        if (countPerThreadString != null) {
            countPerThread = Integer.parseInt(countPerThreadString);
        }
    }

    @After
    public void tearDown() throws Exception {
        runtimeReportsClient.unregisterService(virtualServiceId);
        try {
            runtimeReportsClient.getServiceUsageCount(virtualServiceId);
            fail();
        } catch (RuntimeReportsClientException e) {
        }
    }

    @Test
    public void getResponse_returns_response() throws InterruptedException {
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
        logger.info(String.format("%d simulation calls (%d threads) took: %d ms.", countPerThread * threadsCount, threadsCount, stopWatch.getTime()));

        assertThat(runtimeReportsClient.getServiceUsageCount(virtualServiceId), is(equalTo(countPerThread * threadsCount)));
    }
}