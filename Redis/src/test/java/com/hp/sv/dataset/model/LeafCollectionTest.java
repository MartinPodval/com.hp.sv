package com.hp.sv.dataset.model;

import org.junit.Test;

public class LeafCollectionTest {

    @Test(expected = NullPointerException.class)
    public void ctor() {
        new LeafCollection(null);
    }
}
