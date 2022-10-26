package app;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import Server.SimpleTask;
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
	int rIdx = 0;

	public Simlation(SimpleTask generator, Integer toChange) {
		this.generator = generator;
		this.simStep = 0;
		this.toChange = toChange;
		this.roi = new ArrayList<Double>();
		this.ctime = new ArrayList<Double>();
		// this.dist = new UniformIntegerDistribution(50, 300);
		// this.rates = new int[] { 150, 50, 200, 30, 63 };
		this.rates = new Long[] { 150l, -1000l,150l,50l};
	}

	public void run() {
		this.simStep += 1;
		System.out.println("step=" + this.simStep);
		if (this.simStep % this.toChange == 0) {

			Long rate = null;
			if (this.rates[this.rIdx] < 0) {
				System.out.println("a");
				rate = this.rates[this.rIdx - 1];
				// aggiungo client
				int nclient = Long.valueOf(this.rates[this.rIdx]).intValue() * -1;
				System.out.println(nclient);
				for (int c = 0; c < nclient; c++) {
					this.generator.getThreadpool().submit(new Client(generator, rate));
				}

			} else if (this.rIdx>0 && this.rates[this.rIdx - 1] < 0) {
				System.out.println("b");
				// rimuovo client precedenti
				int nclient = Long.valueOf(this.rates[this.rIdx - 1]).intValue() * -1;
				for (int c = 0; c < nclient; c++) {
					Client.getClients().poll().setDying(true);
				}
				rate = this.rates[this.rIdx];
			} else {
				System.out.println("c");
				rate = this.rates[this.rIdx];
			}

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
