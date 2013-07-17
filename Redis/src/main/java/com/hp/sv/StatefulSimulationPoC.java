package com.hp.sv;

import com.hp.sv.model.TrackPosition;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
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
        final int useCasesCount = 10;
        final int positions = 8;
        final int vsId = 1;
        String vsListName = virtualServicePrefix + ":" + vsId + ":" + trackPositionsListSuffix;

        for(int i = 0; i< useCasesCount; i++) {
            PerformOneCase(vsListName, positions);
        }
    }

    private void PerformOneCase(String vsList, final int positionsCount) {
        // Get
        long[] trackPositionIds = repository.GetList(vsList, 0, -1);
        List<TrackPosition> positions = new ArrayList<>(trackPositionIds.length);
        for (long id : trackPositionIds) {
            positions.add(repository.GetPosition(id));
        }

        logger.debug("Get {} positions.", trackPositionIds.length);

        // Remove
        for(long id: trackPositionIds) {
            repository.RemovePosition(id);
        }
        repository.Remove(vsList);

        logger.debug("{} positions were removed.", trackPositionIds.length);

        // Add new back
        List<TrackPosition> trackPositions = CreatePositions(positionsCount);

        Long[] positionIds = new Long[trackPositions.size()];
        for (int i = 0; i < trackPositions.size(); i++) {
            positionIds[i] = trackPositions.get(i).id;
        }

        repository.AddToList(vsList, positionsCount);
        for (TrackPosition p : trackPositions) {
            repository.AddPosition(p);
        }

        logger.debug("{} positions added.", trackPositionIds.length);
    }

    private static List<TrackPosition> CreatePositions(int count) {
        List<TrackPosition> positions = new ArrayList<>(count);
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            positions.add(new TrackPosition(i, 1, random.nextInt()));
        }

        return positions;
    }
}
