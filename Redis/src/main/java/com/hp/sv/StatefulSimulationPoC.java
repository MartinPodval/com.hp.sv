package com.hp.sv;

import com.hp.sv.model.TrackPosition;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import java.util.Random;

public class StatefulSimulationPoC {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(StatefulSimulationPoC.class.getName());
    private final String virtualServicePrefix = "vs";
    private final String trackPositionsListSuffix = "tp";
    private final Repository repository;

    public StatefulSimulationPoC(String connectionString) {
        repository = new Repository(connectionString);
    }

    public void Dispose() {
        repository.Dispose();
    }

    public void TestStatefulSimulation() {
        final int useCasesCount = 10000;
        final int positions = 8;
        final int vsId = 1;
        String vsListName = virtualServicePrefix + ":" + vsId + ":" + trackPositionsListSuffix;

        repository.ClearAll();

        StopWatch stopWatch = new StopWatch();
        StopWatch readWatch = new StopWatch();
        stopWatch.start();
        readWatch.start();
        readWatch.suspend();

        for (int i = 0; i < useCasesCount; i++) {
            readWatch.resume();
            long[] ids = GetPositions(vsListName);
            readWatch.suspend();

            if (ids.length > 0) {
                for (long id : ids) {
                    repository.RemovePosition(id);
                }
                repository.Remove(vsListName);
            }

            // Add new back
            CreatePositions(vsListName, positions);
        }

        readWatch.stop();
        stopWatch.stop();
        logger.info("{} iterations took {} ms.", useCasesCount, stopWatch.getTime());
        logger.info("Read operations took {} ms.", readWatch.getTime());
    }

    private void CreatePositions(String vsList, int positionsCount) {
        long[] positionIds = new long[positionsCount];

        Random random = new Random();

        for (int i = 0; i < positionsCount; i++) {
            repository.AddPosition(new TrackPosition(i, 1, random.nextInt()));
            positionIds[i] = i;
        }

        repository.AddToList(vsList, positionIds);

        logger.debug("{} positions added.", positionIds.length);
    }

    private long[] GetPositions(String vsList) {
        long[] trackPositionIds = repository.GetList(vsList, 0, 100);

        for (long id : trackPositionIds) {
            // Get ... but discarded immediately
            repository.GetPosition(id);
        }

        logger.debug("Get {} positions.", trackPositionIds.length);

        return trackPositionIds;
    }
}
