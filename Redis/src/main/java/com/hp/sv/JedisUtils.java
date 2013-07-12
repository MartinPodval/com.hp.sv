package com.hp.sv;

import org.apache.logging.log4j.LogManager;
import redis.clients.jedis.Jedis;

public class JedisUtils {

    org.apache.logging.log4j.Logger logger = LogManager.getLogger(JedisUtils.class.getName());

    public void TestWriteAndRead() {
        Jedis connection = new Jedis("localhost");

        connection.set("key", "value");
        String value = connection.get("key");
        logger.info(value);

        connection.quit();
    }
}
