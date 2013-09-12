package com.hp.sv.simulator.simple.test.integration.simulator;


import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
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
  * java "-Dhttp.proxyHost=martinpodval-pc" "-Dhttp.proxyPort=7201" "-DthreadsCount=50" "-Dserver.port=12345" "-DcountPerThread=100" -cp ".;.\dependency\*;.\SimpleSimulatorIntegrationTest-1.0-SNAPSHOT-tests.jar"
 * org.junit.runner.JUnitCore com.hp.sv.simulator.simple.test.integration.simulator.SimulatorImplTest
 * * </code>
 */
//@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class SimulatorImplTest {

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
        final String threadsCountString = System.getProperty("threadsCount");
        if (threadsCountString != null) {
            threadsCount = Integer.parseInt(threadsCountString);
        }
        final String countPerThreadString = System.getProperty("countPerThread");
        if (countPerThreadString != null) {
            countPerThread = Integer.parseInt(countPerThreadString);
        }
    }

    @Test
    public void getResponse_returns_response() throws Exception {
        try(final SimulatorTestCase testCase = new SimulatorTestCase(runtimeReportsClient, simulator, virtualServiceId)) {
            testCase.execute(threadsCount, countPerThread);
        }
    }
}