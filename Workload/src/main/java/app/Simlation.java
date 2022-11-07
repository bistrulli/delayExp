package app;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import Server.SimpleTask;
import redis.clients.jedis.Jedis;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;

public class Simlation implements Runnable {

	SimpleTask generator = null;
	Integer simStep = null;
	Integer toChange = null;
	UniformIntegerDistribution dist = null;
	ArrayList<Double> roi = null;
	ArrayList<Double> ctime = null;
	Long[] rates = null;
	Double[] slas=null;
	int rIdx = 0;
	Jedis j=null;

	public Simlation(SimpleTask generator, Integer toChange) {
		this.generator = generator;
		this.simStep = 0;
		this.toChange = toChange;
		this.roi = new ArrayList<Double>();
		this.ctime = new ArrayList<Double>();
		// this.dist = new UniformIntegerDistribution(50, 300);
		//this.rates = new Long[] { 150l, 50l, 200l, 30l, 63l };
		this.rates = new Long[] { 150l, 150l, 150l};
		// this.rates = new Long[] { 10l,5l,30l,30l};
		//this.slas=new Double[]{0.35,0.55,0.20,0.60,0.25};
		this.slas=new Double[]{0.005,0.25,0.25};
		this.j = new Jedis("localhost");
	}

	public void run() {
		this.simStep += 1;
		System.out.println("step=" + this.simStep);
		if (this.simStep % this.toChange == 0) {
			Long rate = this.rates[this.rIdx];
			this.j.set("N1_sla", this.slas[this.rIdx]+"");

			System.out.println("new Rate=" + rate);
			this.roi.add(Client.getClients().size() / (rate.doubleValue() / 1000.0));
			this.ctime.add(Long.valueOf(System.nanoTime()).doubleValue());
			for (Client c : Client.getClients()) {
				c.setThinkTime(rate.longValue());
			}

			MatFile matFile = Mat5.newMatFile();
			Matrix roiMatrix = Mat5.newMatrix(1, this.roi.size());
			Matrix ctimeMatrix = Mat5.newMatrix(1, this.ctime.size());
			for (int i = 0; i < this.roi.size(); i++) {
				roiMatrix.setDouble(0, i, this.roi.get(i));
				ctimeMatrix.setDouble(0, i, this.ctime.get(i));
			}
			matFile.addArray("roi", roiMatrix);
			matFile.addArray("ctime", ctimeMatrix);
			try {
				Mat5.writeToFile(matFile, "roi_profile.mat");
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.rIdx += 1;
			if (this.rIdx >= this.rates.length) {
				try {
					System.out.println("killing N1");
					Process proc = Runtime.getRuntime().exec("sudo pkill -9 -f N1-0.0.1-jar-with-dependencies.jar");
					proc.waitFor();
					System.out.println("killing N2");
					proc = Runtime.getRuntime().exec("sudo pkill -9 -f N2-0.0.1-jar-with-dependencies.jar");
					proc.waitFor();
					System.out.println("killing Workload");
					proc = Runtime.getRuntime().exec("sudo pkill -9 -f Workload-0.0.1-jar-with-dependencies.jar");
					proc.waitFor();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
