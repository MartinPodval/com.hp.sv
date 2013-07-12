package com.hp.sv;

public class RedisPoC {
    public static void main(String... args) {
        JedisUtils jedisUtils = new JedisUtils();
        jedisUtils.TestWriteAndRead();
    }
}
