package com.hp.sv.repository.api;

interface Repository<T> {
    public T GetById(int id);
}
