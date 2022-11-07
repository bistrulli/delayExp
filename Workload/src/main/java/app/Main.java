package app;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import Server.SimpleTask;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import kong.unirest.Unirest;
import net.spy.memcached.MemcachedClient;

public class Main {
	private static Integer initPop = -1;
	private static String dbHost = null;
	private static String[] systemQueues = null;
	private static File expFile = null;
	private static String tier1Host = null;
	private static boolean sim;
	private static Long aRate = null;

	public static void main(String[] args) {

		System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
		Unirest.config().concurrency(2000, 2000);
		Main.getCliOptions(args);

		if (Main.aRate == null && Main.initPop == null) {
			System.err.println("At least one between aRate and initPop should be set");
		}

		System.out.println(Main.initPop);
		System.out.println(Main.aRate);

		final SimpleTask[] Sys = Main.genSystem();

		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		Simlation sim = new Simlation(Sys[0], 60);
		exec.scheduleAtFixedRate(sim, 0, 1, TimeUnit.SECONDS);

		Sys[0].start();
	}

	public static void resetState(SimpleTask task) {
		MemcachedClient memcachedClient = null;
		try {
			memcachedClient = new MemcachedClient(new InetSocketAddress(Main.dbHost, 11211));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			for (String e : Main.systemQueues) {
				if (e.equals("think")) {
					memcachedClient.set("think", 3600, String.valueOf(0)).get();
				} else {
					if (e.endsWith("_sw") || e.endsWith("_hw")) {
						memcachedClient.set(e, 3600, "100").get();
					} else {
						memcachedClient.set(e, 3600, "0").get();
					}
				}
			}
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		memcachedClient.shutdown();
	}

	public static SimpleTask[] genSystem() {
		HashMap<String, Class> clientEntries = new HashMap<String, Class>();
		HashMap<String, Long> clientEntries_stimes = new HashMap<String, Long>();
		clientEntries.put("think", Client.class);

		int pop = Main.initPop != -1 ? Main.initPop : 1;
		long arate = Main.aRate != null ? Main.aRate : 1000l;

		clientEntries_stimes.put("think", arate);
		final SimpleTask client = new SimpleTask(clientEntries, clientEntries_stimes, pop, "Client", Main.dbHost, null,
				1l);
		Client.setTier1Host(Main.tier1Host);
		return new SimpleTask[] { client };
	}

	public static boolean validate(final String hostname) {
		return InetAddresses.isUriInetAddress(hostname) || InternetDomainName.isValid(hostname);
	}

	public static void getCliOptions(String[] args) {
		int c;
		LongOpt[] longopts = new LongOpt[4];
		longopts[0] = new LongOpt("initPop", LongOpt.REQUIRED_ARGUMENT, null, 0);
		longopts[1] = new LongOpt("dbHost", LongOpt.REQUIRED_ARGUMENT, null, 1);
		longopts[2] = new LongOpt("tier1Host", LongOpt.REQUIRED_ARGUMENT, null, 2);
		longopts[3] = new LongOpt("aRate", LongOpt.REQUIRED_ARGUMENT, null, 3);

		Getopt g = new Getopt("ddctrl", args, "", longopts);
		g.setOpterr(true);
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				try {
					Main.initPop = Integer.valueOf(g.getOptarg());
				} catch (NumberFormatException e) {
					System.err.println(String.format("%s is not valid, it must be 0 or 1.", g.getOptarg()));
				}
				break;
			case 1:
				try {
					if (!Main.validate(g.getOptarg())) {
						throw new Exception(String.format("%s is not a valid jedis URL", g.getOptarg()));
					}
					Main.dbHost = String.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					Main.tier1Host = String.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					Main.aRate = Long.valueOf(g.getOptarg());
					if (aRate < 0) {
						throw new Exception();
					}
				} catch (Exception e) {
					System.err.println(String.format("%s is not valid, it must be a Long number >0.", g.getOptarg()));
				}
				break;
			default:
				break;
			}
		}
	}
}
