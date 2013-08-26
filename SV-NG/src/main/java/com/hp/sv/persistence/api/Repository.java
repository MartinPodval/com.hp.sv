package com.hp.sv.persistence.api;

interface Repository<T> {
    public T GetById(int id);
}
