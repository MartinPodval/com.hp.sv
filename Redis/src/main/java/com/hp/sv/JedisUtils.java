package com.hp.sv;

import com.hp.sv.model.TrackPosition;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class JedisUtils {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(JedisUtils.class.getName());
    private static String incrementName = "myIncrement";
    private static String collectionName = "myList";
    private static String vsCounter = "vsCnt";
    private static String vsPrefix = "vs";
    private static String tpList = "tpList";
    private final Repository repository;

    public JedisUtils(String connectionString) {
        repository = new Repository(connectionString);
    }

    public void Dispose() {
        repository.Dispose();
    }

    public void TestWriteAndRead() {
        final int vsCount = 10000;
        final int trackPositionsCount = 100;
        final int readCount = 10;

        repository.ClearAll();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Long> vsIds = CreateList(vsCount, trackPositionsCount);

        stopWatch.stop();
        logger.debug("Total time [ms]: " + stopWatch.getTime());

        Collections.shuffle(vsIds, new Random());

        stopWatch.reset();
        stopWatch.start();

        for (int i = 0; i < readCount; i++) {
            for (Long vsId : vsIds) {
                GetListItems(vsId);
            }
        }

        stopWatch.stop();
        logger.debug("Total time [ms]: " + stopWatch.getTime());
        logger.debug("Cycle were repeated {} times, {} virtual services, {} track positions.", readCount, vsCount, trackPositionsCount);
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

    private void GetListItems(long vsId) {
        String listName = vsPrefix + vsId + tpList;
        long[] tpIds = repository.GetList(listName, 1, 1);
        Validate.isTrue(tpIds.length == 1);

        for (long tpId : tpIds) {
            TrackPosition position = repository.GetPosition(tpId);
            Validate.notNull(position);
            Validate.isTrue(position.id == tpId);
        }
    }
}
