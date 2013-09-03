package com.hp.sv.runtime.reports.inmemory.rest.server;

import com.hp.sv.runtime.reports.inmemory.rest.service.RuntimeReportRestfulServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class Server {
    private static final Log logger = LogFactory.getLog(Server.class);

    public static void main(String... args) throws IOException {
        logger.info("Starting server ... ");
        HttpServer httpServer = null;

        try {
            final ResourceConfig config = getResourceConfig();
            final URI uri = UriBuilder.fromUri("http://localhost").port(9998).build();
            httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, config, true);

            logger.info("Server has started. Press any key to stop.");
            System.in.read();
            logger.info("Server is about to stop.");
        } finally {
            if (httpServer != null) {
                httpServer.stop();
            }
        }
    }

    private static ResourceConfig getResourceConfig() {
        ResourceConfig rc = new ResourceConfig()
                .register(SpringLifecycleListener.class)
                .register(RequestContextFilter.class);
        rc.property("contextConfigLocation", "classpath*:/spring/config.xml");
        rc.register(RuntimeReportRestfulServiceImpl.class);

        return rc;
    }
}
