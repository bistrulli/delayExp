package jedis;

import redis.clients.jedis.JedisPubSub;

public class KeyListener extends JedisPubSub {

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		System.out.println("onPSubscribe " + pattern + " " + subscribedChannels);
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		//System.out.println("onPMessage pattern " + pattern + " " + channel + " " + message);
		if(message.equals("set")) {
			System.out.println("detected");
		}
	}
}