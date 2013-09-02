package com.hp.sv.simulator.simple.test.integration.simulator;

import com.hp.sv.runtime.reports.api.RuntimeReportsClient;
import com.hp.sv.simulator.api.simulator.Simulator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.core.Application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SimulatorImplTest extends JerseyTest {

    @Autowired
    protected Simulator simulator;

    @Autowired
    @Qualifier("runtimeReportClient")
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

    @Override
    protected Application configure() {
        ResourceConfig rc = new ResourceConfig()
                .register(SpringLifecycleListener.class)
                .register(RequestContextFilter.class)
                ;
        rc.property("contextConfigLocation", "classpath*:/spring/config.xml");
        return rc.register(SimulatorImplTest.class);
    }

    @Test
    public void getResponse_returns_response() {

        assertThat(simulator.getResponse(new Object(), virtualServiceId), is(nullValue()));
    }
}
