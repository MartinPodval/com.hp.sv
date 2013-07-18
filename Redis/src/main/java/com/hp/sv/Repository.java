package com.hp.sv;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.sv.dataset.model.Leaf;
import com.hp.sv.dataset.model.ServiceCall;
import com.hp.sv.track.model.TrackPosition;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Repository {
    public static final String trackPosition = "tp";
    public static final String trackSuffix = "trc";
    public static final String costSuffix = "cst";
    public static final String serviceCallName = "sc";
    public static final String leafName = "lf";
    public static final String counter = "cnt";
    public static final String type = "tp";
    public static final String value = "v";
    public static final int KryoBufferSize = 25000;
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(Repository.class.getName());
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

        connection.set(trackPosition + position.id + trackSuffix, String.valueOf(position.trackId));
        connection.set(trackPosition + position.id + costSuffix, String.valueOf(position.cost));
    }

    public TrackPosition GetPosition(long positionId) {
        long trackId = Long.parseLong(connection.get(trackPosition + positionId + trackSuffix));
        int cost = Integer.parseInt(connection.get(trackPosition + positionId + costSuffix));
        return new TrackPosition(positionId, trackId, cost);
    }

    public void Remove(String key) {
        Validate.notEmpty(key);
        Validate.isTrue(connection.del(key) == 1);
    }

    public void RemovePosition(long id) {
        Validate.isTrue(id >= 0);
        Long numberOfRemovedKeys = connection.del(trackPosition + id + trackSuffix, trackPosition + id + costSuffix);
        Validate.isTrue(numberOfRemovedKeys == 2);
    }

    public void AddToSet(String setName, double score, String value) {
        Validate.notEmpty(setName);
        Validate.notEmpty(value);

        connection.zadd(setName, score, value);
    }

    public Set<String> GetFromSet(String setName, long start, long end) {
        return connection.zrevrange(setName, start, end);
    }

    public void RemoveSet(String setName) {
        Validate.notEmpty(setName);
        connection.del(setName);
    }

    public Long SizeOfList(String listName) {
        Validate.notEmpty(listName);
        return connection.llen(listName);
    }

    public void AddServiceCallAsKeyValuePairs(ServiceCall serviceCall) {
        Validate.notNull(serviceCall);

        Set<Map.Entry<Long, Leaf>> entries = serviceCall.GetLeaves();
        long[] leafIds = new long[entries.size()];

        int count = 0;
        for (Map.Entry<Long, Leaf> entry : entries) {
            leafIds[count] = entry.getKey();
            count++;
            AddLeaf(entry.getValue());
        }

        AddToList(serviceCallName + ":" + serviceCall.getId(), leafIds);
    }

    public void AddServiceCallAsExternalizableByteArray(ServiceCall serviceCall) throws IOException {
        Validate.notNull(serviceCall);
        byte[] output = null;

        ByteArrayOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(serviceCall);
            objectOutputStream.flush();

            output = outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("", e);
            throw e;

        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                    throw e;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                    throw e;
                }
            }
        }

        connection.set((serviceCallName + ":" + serviceCall.getId()).getBytes(), output);
    }

    public void AddServiceCallAsKryoByteArray(ServiceCall serviceCall, Kryo kryo) {
        Validate.notNull(serviceCall);
        Validate.notNull(kryo);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream, KryoBufferSize);
        kryo.writeObject(output, serviceCall);
        output.flush();
        output.close();
        byte[] byteArrayOutput = outputStream.toByteArray();

        if (serviceCall.getId() == 15000) {
            logger.info(byteArrayOutput.length);
        }

        connection.set((serviceCallName + ":" + serviceCall.getId()).getBytes(), byteArrayOutput);
    }

    public void AddLeaf(Leaf leaf) {
        Validate.notNull(leaf);

        connection.set(leafName + ":" + leaf.getId() + ":" + type, String.valueOf(leaf.getType().ordinal()));
        connection.set(leafName + ":" + leaf.getId() + ":" + value, leaf.getValue());
    }

    public ServiceCall GetServiceCallAsKeyValuePairs(Long id) {
        Validate.notNull(id);
        ServiceCall serviceCall = new ServiceCall(id);

        String listName = serviceCallName + ":" + id;
        long[] leafIds = GetList(listName, 0, Integer.MAX_VALUE);

        for (long leafId : leafIds) {
            serviceCall.Add(GetLeaf(leafId));
        }

        return serviceCall;
    }

    public ServiceCall GetServiceCallAsExternalizableByteArray(Long id) throws ClassNotFoundException, IOException {
        Validate.notNull(id);

        ServiceCall serviceCall = null;

        byte[] bytes = connection.get((serviceCallName + ":" + id).getBytes());
        Validate.notNull(bytes);

        ByteArrayInputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            inputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(inputStream);
            serviceCall = (ServiceCall) objectInputStream.readObject();
        } catch (IOException e) {
            logger.error("", e);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            throw e;
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                    throw e;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                    throw e;
                }
            }
        }

        return serviceCall;
    }

    public ServiceCall GetServiceCallAsKryoByteArray(Long id, Kryo kryo) {
        Validate.notNull(id);
        Validate.notNull(kryo);

        byte[] bytes = connection.get((serviceCallName + ":" + id).getBytes());
        Validate.notNull(bytes);
        Validate.isTrue(bytes.length > 0);

        Input input = new Input(bytes);
        ServiceCall serviceCall = kryo.readObject(input, ServiceCall.class);
        input.close();

        return serviceCall;
    }

    public Leaf GetLeaf(long id) {
        Leaf.Type leafType = Leaf.Type.values()[Integer.valueOf(connection.get(leafName + ":" + id + ":" + type))];
        String leafValue = connection.get(leafName + ":" + id + ":" + value);
        return new Leaf(id, leafType, leafValue);
    }

    public void Dispose() {
        connection.quit();
    }
}
