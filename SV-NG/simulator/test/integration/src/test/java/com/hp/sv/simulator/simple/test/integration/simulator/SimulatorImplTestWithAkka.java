package com.hp.sv.simulator.simple.test.integration.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;

@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulatorImplTestWithAkka {

    private static final Log logger = LogFactory.getLog(SimulatorImplTestWithAkka.class);

    @Autowired
    protected Simulator simulator;

    @Autowired
    @Qualifier("akkaRuntimeReportClient")
    protected RuntimeReportsClient runtimeReportsClient;

    protected static int threadsCount = 10;
    protected static int countPerThread = 500000;

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
        try (final SimulatorTestCase testCase = new SimulatorTestCase(runtimeReportsClient, simulator, virtualServiceId)) {
            Thread.sleep(100);
            logger.info("After init");
            testCase.execute(threadsCount, countPerThread);
        }
    }
}
