package com.hp.sv.runtime.reports.inmemory.rest.service;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

public class MyApplication extends ResourceConfig {

    public MyApplication() {
        register(RequestContextFilter.class);
        register(RuntimeReportRestfulServiceImpl.class);
    }
}