package com.hp.sv;

import com.hp.sv.model.TrackPosition;
import org.apache.commons.lang3.Validate;
import redis.clients.jedis.Jedis;

import java.util.List;

public class Repository {
    private final String trackPositionPrefix = "tp";
    private final String trackSuffix = "trc";
    private final String costSuffix = "cst";
    private final Jedis connection;

    public Repository(String connectionString) {
        Validate.notEmpty(connectionString);
        connection = new Jedis(connectionString);
    }

    public void ClearAll() {
        connection.flushAll();
    }

    public long Inc(String counterName) {
        return connection.incr(counterName);
    }

    public void AddToList(String listName, long... ids) {
        Validate.notEmpty(listName);
        Validate.isTrue(ids.length > 0);

        String[] stringIds = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            stringIds[i] = String.valueOf(ids[i]);
        }

        connection.lpush(listName, stringIds);
    }

    public long[] GetList(String listName, int index, int size) {
        Validate.notEmpty(listName);
        Validate.isTrue(index >= 0);
        Validate.isTrue(size > 0);

        List<String> stringIds = connection.lrange(listName, index, index + size - 1);
        long[] ids = new long[stringIds.size()];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.parseLong(stringIds.get(i));
        }

        return ids;
    }

    public void AddPosition(TrackPosition position) {
        Validate.notNull(position);

        connection.set(trackPositionPrefix + position.id + trackSuffix, String.valueOf(position.trackId));
        connection.set(trackPositionPrefix + position.id + costSuffix, String.valueOf(position.cost));
    }

    public TrackPosition GetPosition(long positionId) {
        long trackId = Long.parseLong(connection.get(trackPositionPrefix + positionId + trackSuffix));
        int cost = Integer.parseInt(connection.get(trackPositionPrefix + positionId + costSuffix));
        return new TrackPosition(positionId, trackId, cost);
    }

    public Long SizeOfList(String listName) {
        Validate.notEmpty(listName);
        return connection.llen(listName);
    }

    public void Dispose() {
        connection.quit();
    }
}
