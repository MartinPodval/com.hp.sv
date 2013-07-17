package com.hp.sv;

import org.apache.logging.log4j.LogManager;

public class RedisPoC {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(RedisPoC.class.getName());

    public static void main(String... args) {
        String connectionString = GetRedisConnectionString(args);
        logger.info("Using \"{}\" as connection string.", connectionString);

        /*TrackPerformancePositionPoC trackPositionPoC = new TrackPerformancePositionPoC(connectionString);
        trackPositionPoC.TestWriteAndRead();
        trackPositionPoC.Dispose();*/

        /*SortedSetPoC sortedSetPoC = new SortedSetPoC(connectionString);
        sortedSetPoC.Test();
        sortedSetPoC.Dispose();*/

        StatefulSimulationPoC sortedSetPoC = new StatefulSimulationPoC(connectionString);
        sortedSetPoC.TestStatefulSimulation();
        sortedSetPoC.Dispose();
    }

    private static String GetRedisConnectionString(String[] args) {
        return args.length < 1 ? "localhost" : args[0];
    }
}
