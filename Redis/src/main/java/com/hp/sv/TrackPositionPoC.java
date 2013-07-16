package com.hp.sv;

import com.hp.sv.model.TrackPosition;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrackPositionPoC {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(TrackPositionPoC.class.getName());
    private static String vsCounter = "vsCnt";
    private static String vsPrefix = "vs";
    private static String tpList = "tpList";
    private final Repository repository;

    public TrackPositionPoC(String connectionString) {
        repository = new Repository(connectionString);
    }

    public void Dispose() {
        repository.Dispose();
    }

    public void TestWriteAndRead() {
        final int vsCount = 100;
        final int trackPositionsCount = 100;
        final int readVsCount = 10;
        final int readTpCount = 8;

        repository.ClearAll();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Long> vsIds = CreateList(vsCount, trackPositionsCount);

        stopWatch.stop();
        logger.debug("Total time [ms]: " + stopWatch.getTime());

        Collections.shuffle(vsIds, new Random());

        stopWatch.reset();
        stopWatch.start();

        for (int i = 0; i < readVsCount; i++) {
            for (Long vsId : vsIds) {
                GetListItems(vsId, readTpCount);
            }
        }

        stopWatch.stop();
        logger.debug("Total time [ms]: " + stopWatch.getTime());
        logger.debug("Cycle was repeated {} times, {} virtual services, {}/{} (read/write) track positions.", readVsCount, vsCount, readTpCount, trackPositionsCount);
    }

    private List<Long> CreateList(int vsCount, int trackPositionsCount) {
        List<Long> vsIds = new ArrayList<>(vsCount);

        for (int i = 0; i < vsCount; i++) {
            vsIds.add(repository.Inc(vsCounter));

            TrackPosition[] positions = new TrackPosition[trackPositionsCount];
            long[] tpIds = new long[positions.length];

            for (int j = 0; j < trackPositionsCount; j++) {
                positions[j] = new TrackPosition(j, Long.valueOf(i), 13);
                tpIds[j] = j;
                repository.AddPosition(positions[j]);
            }

            String listName = vsPrefix + vsIds.get(i) + tpList;
            repository.AddToList(listName, tpIds);
            logger.debug("Size of {} is {} for virtual service [Id={}].", listName, repository.SizeOfList(listName), i);
        }
        return vsIds;
    }

    private void GetListItems(long vsId, int count) {
        String listName = vsPrefix + vsId + tpList;
        long[] tpIds = repository.GetList(listName, 1, count);
        Validate.isTrue(tpIds.length == count);

        for (long tpId : tpIds) {
            TrackPosition position = repository.GetPosition(tpId);
            Validate.notNull(position);
            Validate.isTrue(position.id == tpId);
        }
    }
}
