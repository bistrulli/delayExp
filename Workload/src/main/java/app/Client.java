package app;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import Server.SimpleTask;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import monitoring.rtSample;
import net.spy.memcached.MemcachedClient;

public class Client implements Runnable {

	private SimpleTask task = null;
	private ExponentialDistribution dist = null;
	private long thinkTime = -1;
	private UUID clietId = null;
	public static AtomicInteger time = new AtomicInteger(0);
	private MemcachedClient memcachedClient = null;
	private static AtomicInteger toKill = new AtomicInteger(0);
	private Boolean dying = null;
	private static String tier1Host = null;
	public static AtomicBoolean isStarted = new AtomicBoolean(false);

	public Client(SimpleTask task, Long ttime) {
		this.setThinkTime(ttime);
		this.task = task;
		this.clietId = UUID.randomUUID();
		this.dying = false;
	}

	public void run() {
		try {

			HttpResponse<String> resp = null;
			int thinking = this.task.getState().get("think").incrementAndGet();
			//CompletableFuture<HttpResponse<String>> resp=null;

			while (!this.dying) {

				this.task.getEnqueueTime().put(this.clietId.toString(), System.nanoTime());
				SimpleTask.getLogger().debug(String.format("%s thinking", thinking));
				TimeUnit.MILLISECONDS.sleep(Double.valueOf(this.dist.sample()).longValue());

				SimpleTask.getLogger().debug(String.format("%s sending", this.task.getName()));
				this.task.getState().get("think").decrementAndGet();

				resp = Unirest.get(URI.create("http://" + Client.getTier1Host() + ":3100/?id=" + this.clietId.toString()
						+ "&entry=e1" + "&snd=think").toString()).header("Connection", "close").asString();
				
//				this.clietId = UUID.randomUUID();
//				Unirest.get(URI.create("http://" + Client.getTier1Host() + ":3100/?id=" + this.clietId.toString()
//				+ "&entry=e1" + "&snd=think").toString()).header("Connection", "close").asStringAsync();
				
				
				thinking = this.task.getState().get("think").incrementAndGet();
				
				this.task.getRts().addSample(new rtSample(this.task.getEnqueueTime().get(this.clietId.toString()),
						System.nanoTime()));
				
				System.out.println("request");
			}
			SimpleTask.getLogger().debug(String.format(" user %s stopped", this.clietId));
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.memcachedClient.shutdown();
		}
	}

	public long getThinkTime() {
		return this.thinkTime;
	}

	public AbstractRealDistribution getTtimeDist() {
		return this.dist;
	}

	public void setThinkTime(long thinkTime) {
		this.thinkTime = thinkTime;
		this.dist = new ExponentialDistribution(thinkTime);
	}

	public static AtomicInteger getToKill() {
		return toKill;
	}

	public static void setToKill(Integer toKill) {
		Client.toKill.set(toKill);
	}

	public static String getTier1Host() {
		return tier1Host;
	}

	public static void setTier1Host(String tier1Host) {
		Client.tier1Host = tier1Host;
	}

}
