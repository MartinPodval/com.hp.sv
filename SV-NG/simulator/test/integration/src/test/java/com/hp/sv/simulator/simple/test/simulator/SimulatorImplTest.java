package com.hp.sv.simulator.simple.test.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsService;
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
    protected RuntimeReportsService runtimeReportsService;

    private int virtualServiceId = 123;

    @Before
    public void setUp() throws Exception {
        runtimeReportsService.registerService(virtualServiceId);
    }

    @After
    public void tearDown() throws Exception {
        runtimeReportsService.unregisterService(virtualServiceId);
    }

    @Test
    public void getResponse_returns_response() {
        assertThat(simulator.getResponse(new Object(), virtualServiceId), is(nullValue()));
    }
}
