package com.hp.sv.dataset.model;

import org.junit.Test;

public class LeafTest {

    @Test(expected = NullPointerException.class)
    public void ctor_id() throws Exception {
        new Leaf(null, null, "");
    }

    @Test(expected = NullPointerException.class)
    public void ctor_type() throws Exception {
        new Leaf(1L, null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctor_value() throws Exception {
        new Leaf(1L, Leaf.Type.Second, "");
    }
}
