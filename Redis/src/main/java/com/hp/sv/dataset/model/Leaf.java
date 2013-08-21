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

public class Leaf implements Externalizable, KryoSerializable {
    private Long id;
    private Type type;
    private String value;

    public Leaf() {
    }

    public Leaf(Long id, Type type, String value) {
        Validate.notNull(id);
        Validate.notNull(type);
        Validate.notEmpty(value);

        this.id = id;
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
        out.writeInt(type.ordinal());
        out.writeObject(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readLong();
        type = Type.values()[in.readInt()];
        value = (String) in.readObject();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id);
        output.writeInt(type.ordinal());
        output.writeString(value);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong();
        type = Type.values()[input.readInt()];
        value = input.readString();
    }

    public enum Type {
        First,
        Second,
        Third
    }
}
