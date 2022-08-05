package jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Subscriber {
	public static void main(String[] args) {
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

		Jedis jedis = pool.getResource();
		jedis.psubscribe(new KeyListener(), "__key*__:N1_rt");
		jedis.close();
	}

}