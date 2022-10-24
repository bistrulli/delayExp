package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import Server.SimpleTask;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import jni.GetThreadID;
import kong.unirest.Unirest;
import net.spy.memcached.MemcachedClient;

public class Main {

	private static Boolean isEmu = false;
	private static String dbHost = null;
	private static boolean cgv2 = false;
	private static Integer port = 3000;
	private static String name;
	private static Float alfa;
	private static Float tgt;
	private static Integer nr;

	public static void main(String[] args) {
		System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
		Unirest.config().concurrency(2000, 2000);
		Main.getCliOptions(args);
		if (Main.cgv2) {
			try {
				Main.addToCgv2();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		SimpleTask[] Sys = Main.genSystem();
		Sys[0].start();
	}

	public static void resetState(SimpleTask task) throws InterruptedException, ExecutionException {
		MemcachedClient memcachedClient = null;
		try {
			memcachedClient = new MemcachedClient(new InetSocketAddress(Main.dbHost, 11211));
		} catch (IOException e) {
			e.printStackTrace();
		}
		memcachedClient.set(task.getName() + "_sw", 3600, "1").get();
		memcachedClient.set(task.getName() + "_hw", 3600, "1").get();
		String[] entries = task.getEntries().keySet().toArray(new String[0]);
		for (String e : entries) {
			memcachedClient.set(e + "_bl", Integer.MAX_VALUE, "0").get();
			memcachedClient.set(e + "_ex", Integer.MAX_VALUE, "0").get();
		}
		memcachedClient.shutdown();
	}

	public static SimpleTask[] genSystem() {
		HashMap<String, Class> tEntries = new HashMap<String, Class>();
		HashMap<String, Long> tEntries_stimes = new HashMap<String, Long>();
		tEntries.put("e2", N2HTTPHandler.class);
		tEntries_stimes.put("e2", 150l);
		final SimpleTask N = new SimpleTask("localhost", Main.port, tEntries, tEntries_stimes, 30, Main.isEmu,
				Main.name, Main.dbHost, 1l, 1l, null, Main.cgv2);
		N.setHwCore(1f);
		N.getCtrl().setNr(Main.nr);
		N.getCtrl().setAlpha(Main.alfa);
		N.getCtrl().setTauro(Main.tgt); 
		
		return new SimpleTask[] { N };
	}

	public static boolean validate(final String hostname) {
		return InetAddresses.isUriInetAddress(hostname) || InternetDomainName.isValid(hostname);
	}

	private static Boolean isCgroupV2Enabled() {
		File cgfile = new File("/sys/fs/cgroup/" + Main.name + "/cgroup.procs");
		return cgfile.exists();
	}

	public static void addToCgv2() throws Exception {
		if (Main.isCgroupV2Enabled()) {
			try {
				int tid = GetThreadID.get_tid();
				// aggiungo questo thread al gruppo dei serventi del tier
				BufferedWriter out;
				try {
					out = new BufferedWriter(new FileWriter("/sys/fs/cgroup/" + Main.name + "/cgroup.procs", true));
					out.write(String.valueOf(tid));
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new Exception("Cgroupv2 is not enabled");
		}
	}

	public static void getCliOptions(String[] args) {

		int c;
		LongOpt[] longopts = new LongOpt[8];
		longopts[0] = new LongOpt("cpuEmu", LongOpt.REQUIRED_ARGUMENT, null, 0);
		longopts[1] = new LongOpt("dbHost", LongOpt.REQUIRED_ARGUMENT, null, 1);
		longopts[2] = new LongOpt("cgv2", LongOpt.REQUIRED_ARGUMENT, null, 2);
		longopts[3] = new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 3);
		longopts[4] = new LongOpt("name", LongOpt.REQUIRED_ARGUMENT, null, 4);
		longopts[5] = new LongOpt("alfa", LongOpt.REQUIRED_ARGUMENT, null, 5);
		longopts[6] = new LongOpt("tgt", LongOpt.REQUIRED_ARGUMENT, null, 6);
		longopts[7] = new LongOpt("nr", LongOpt.REQUIRED_ARGUMENT, null, 7);

		Getopt g = new Getopt("ddctrl", args, "", longopts);
		g.setOpterr(true);
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				try {
					Main.isEmu = Integer.valueOf(g.getOptarg()) > 0 ? true : false;
				} catch (NumberFormatException e) {
					System.err.println(String.format("%s is not valid, it must be 0 or 1.", g.getOptarg()));
				}
				break;
			case 1:
				try {
					if (!Main.validate(g.getOptarg())) {
						throw new Exception(String.format("%s is not a valid db HOST", g.getOptarg()));
					}
					Main.dbHost = String.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					Main.cgv2 = Integer.valueOf(g.getOptarg()) > 0 ? true : false;
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					Main.port = Integer.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 4:
				try {
					Main.name = String.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 5:
				try {
					Main.alfa = Float.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 6:
				try {
					Main.tgt = Float.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 7:
				try {
					Main.nr = Integer.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

}
