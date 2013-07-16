package com.hp.sv;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class SortedSetPoC {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(SortedSetPoC.class.getName());
    private static String setName = "set";
    private final Repository repository;

    public SortedSetPoC(String connectionString) {
        repository = new Repository(connectionString);
    }

    public void Dispose() {
        repository.Dispose();
    }

    public void Test() {
        final int count = 1000000;
        Random random = new Random();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < count; i++) {
            double score = random.nextDouble();
            repository.AddToSet(setName, score, String.valueOf(score));
            if (i % 10000 == 0) {
                logger.debug("{} item added.", i);
            }
        }

        stopWatch.stop();
        logger.debug("Total time of add [ms]: " + stopWatch.getTime());

        stopWatch.reset();
        stopWatch.start();
        Set<String> scores = repository.GetFromSet(setName, 100, 200);
        stopWatch.stop();
        logger.debug("<100, 200> [ms]: " + stopWatch.getTime());
        for(String s : scores) logger.debug(s);


        stopWatch.reset();
        stopWatch.start();
        repository.GetFromSet(setName, 55000, 56000);
        stopWatch.stop();
        logger.debug("<55000, 56000> [ms]: " + stopWatch.getTime());

    }
}
