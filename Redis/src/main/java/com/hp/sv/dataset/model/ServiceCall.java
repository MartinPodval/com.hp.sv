package com.hp.sv.dataset.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.Validate;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceCall implements Externalizable, KryoSerializable {

    private Long id;
    private Map<Long, Leaf> leaves;

    public ServiceCall() {
    }

    public ServiceCall(Long id) {
        this.id = id;
        leaves = new HashMap<>();
    }

    public void Add(Leaf leaf) {
        Validate.notNull(leaf);
        leaves.put(leaf.getId(), leaf);
    }

    public Set<Map.Entry<Long, Leaf>> GetLeaves() {
        return leaves.entrySet();
    }

    public Long getId() {
        return id;
    }

    public int GetLeavesCount() {
        return leaves.size();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
        LeafCollection collection = new LeafCollection(leaves.values());
        out.writeObject(collection);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readLong();
        leaves = new HashMap<>();
        LeafCollection collection = (LeafCollection) in.readObject();
        for (Leaf leaf : collection.GetLeaves()) {
            leaves.put(leaf.getId(), leaf);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id);
        LeafCollection collection = new LeafCollection(leaves.values());
        kryo.writeObject(output, collection);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong();
        leaves = new HashMap<>();
        LeafCollection collection = kryo.readObject(input, LeafCollection.class);

        for (Leaf leaf : collection.GetLeaves()) {
            leaves.put(leaf.getId(), leaf);
        }
    }
}
