package com.hp.sv;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;

public class RedisPoC {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(RedisPoC.class.getName());

    public static final String RedisHost = "redisHost";
    public static final String Threads = "threads";
    public static final String Localhost = "localhost";

    public static void main(String... args) throws ParseException {
        Options options = new Options();
        options.addOption(RedisHost, true, "Redis host name");
        options.addOption(Threads, true, "Number of threads");

        CommandLineParser parser = new BasicParser();
        CommandLine parse = parser.parse(options, args);

        String redisHost;
        if(parse.hasOption(RedisHost)) {
            redisHost = parse.getOptionValue(RedisHost);
        }
        else {
            redisHost = Localhost;
        }

        int threads = 1;
        if(parse.hasOption(Threads)) {
            threads = Integer.valueOf(parse.getOptionValue(Threads));
        }

        logger.info("Using \"{}\" as connection string. Number of clients {}", redisHost, threads);

        /*TrackPerformancePositionPoC trackPositionPoC = new TrackPerformancePositionPoC(connectionString);
        trackPositionPoC.TestWriteAndRead();
        trackPositionPoC.Dispose();*/

        /*SortedSetPoC sortedSetPoC = new SortedSetPoC(connectionString);
        sortedSetPoC.Test();
        sortedSetPoC.Dispose();*/

        RunStatefulSimulationPoC(redisHost, threads);
    }

    private static void RunStatefulSimulationPoC(final String connectionString, int clients) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StatefulSimulationPoC sortedSetPoC = new StatefulSimulationPoC(connectionString);
                sortedSetPoC.TestStatefulSimulation();
                sortedSetPoC.Dispose();
            }
        };

        Thread[] ts = new Thread[clients];
        for (int i = 0; i < clients; i++) {
            ts[i] = new Thread(runnable);
        }

        for (Thread t : ts) {
            t.start();
        }

        for (Thread t : ts) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }
}
