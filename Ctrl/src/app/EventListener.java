package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class EventListener extends JedisPubSub {

	private File logFile = null;
	private FileWriter logW = null;
	private JedisPool pool;

	public EventListener() {
		this.logFile = new File(String.format("%s_t2.log", "Ctrl"));
		this.pool = new JedisPool(new JedisPoolConfig(), "localhost");
		try {
			this.logW = new FileWriter(this.logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		switch (message) {
		case "set": {
			try {
				this.logW.write(System.nanoTime() + "\n");
				this.logW.flush();
				Jedis j = this.pool.getResource();
				j.set("N1_hw","1");
				j.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		}
	}
}