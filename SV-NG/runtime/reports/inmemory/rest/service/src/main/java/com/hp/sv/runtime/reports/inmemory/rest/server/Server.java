package com.hp.sv.runtime.reports.inmemory.rest.server;

import com.hp.sv.runtime.reports.inmemory.rest.service.RuntimeReportRestfulServiceImpl;
import org.apache.commons.cli.*;
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
    public static final String urlOptionName = "u";
    public static final String portOptionName = "p";

    public static void main(String... args) throws IOException {
        String url = "";
        long port = 0;

        final Options options = getOptions();

        logger.info("Starting server ... ");
        CommandLineParser parser = new BasicParser();
        try {
            final CommandLine line = parser.parse(options, args);
            url = line.getOptionValue(urlOptionName);
            port = (Long) line.getParsedOptionValue(portOptionName);

            HttpServer httpServer = null;

            try {
                final ResourceConfig config = getResourceConfig();
                final URI uri = UriBuilder.fromUri(url).port((int)port).build();
                httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, config, true);

                logger.info("Server has started. Press any key to stop.");
                System.in.read();
                logger.info("Server is about to stop.");
            } finally {
                if (httpServer != null) {
                    httpServer.stop();
                }
            }

        } catch (ParseException e) {
            logger.error(e);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Runtime Report InMemory Rest Service", options);
        }
    }

    private static ResourceConfig getResourceConfig() {
        return new ResourceConfig()
                .register(SpringLifecycleListener.class)
                .register(RequestContextFilter.class)
                .property("contextConfigLocation", "classpath*:/spring/config.xml")
                .register(RuntimeReportRestfulServiceImpl.class);
    }

    private static Options getOptions() {
        final Options options = new Options();

        final Option url = new Option(urlOptionName, "url", true, "server url");
        url.setRequired(true);
        url.setType(String.class);
        options.addOption(url);

        final Option port = new Option(portOptionName, "port", true, "server port");
        port.setRequired(true);
        port.setType(Number.class);
        options.addOption(port);

        return options;
    }
}
