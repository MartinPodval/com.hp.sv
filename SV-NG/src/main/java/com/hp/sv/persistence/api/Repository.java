package com.hp.sv.persistence.api;

public interface Repository<T> {
    public T GetById(int id);
}
