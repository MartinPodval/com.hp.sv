package com.hp.sv.simulator.simple.test.integration.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(locations = {"classpath*:/spring/config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulatorImplTest {

    @Autowired
    protected Simulator simulator;

    @Autowired
    protected RuntimeReportsClient runtimeReportsClient;

    private int virtualServiceId = 123;

    @Before
    public void setUp() throws Exception {
        runtimeReportsClient.registerService(virtualServiceId);
    }

    @After
    public void tearDown() throws Exception {
        runtimeReportsClient.unregisterService(virtualServiceId);
    }

    @Test
    public void getResponse_returns_response() {
        assertThat(simulator.getResponse(new Object(), virtualServiceId), is(nullValue()));
    }
}
