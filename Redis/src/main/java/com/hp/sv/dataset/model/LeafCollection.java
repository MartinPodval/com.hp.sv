package com.hp.sv.dataset.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LeafCollection implements Externalizable, KryoSerializable {
    private List<Leaf> leaves;

    public LeafCollection() {
    }

    public LeafCollection(Collection<Leaf> leaves) {
        this.leaves = new ArrayList<>(leaves);
    }

    public Collection<Leaf> GetLeaves() {
        return leaves;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(leaves.size());
        for (Leaf leaf : leaves) {
            out.writeObject(leaf);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        leaves = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Leaf leaf = (Leaf) in.readObject();
            leaves.add(leaf);
        }
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(leaves.size());
        for (Leaf leaf : leaves) {
            kryo.writeObject(output, leaf);
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int size = input.readInt();
        leaves = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Leaf leaf = kryo.readObject(input, Leaf.class);
            leaves.add(leaf);
        }
    }
}
