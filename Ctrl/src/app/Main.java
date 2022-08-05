package app;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Main {
	public static void main(String[] args) {
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

		Jedis jedis = pool.getResource();
		jedis.psubscribe(new EventListener(), "__key*__:N1_rt");
		jedis.close();
	}
}
