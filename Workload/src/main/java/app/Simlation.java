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
	int[] rates = null;
	int rIdx=0;

	public Simlation(SimpleTask generator, Integer toChange) {
		this.generator = generator;
		this.simStep = 0;
		this.toChange = toChange;
		this.roi = new ArrayList<Double>();
		this.ctime = new ArrayList<Double>();
		// this.dist = new UniformIntegerDistribution(50, 300);
		this.rates = new int[] { 300, 50, 100, 200 };

	}

	public void run() {
		this.simStep += 1;
		System.out.println("step=" + this.simStep);
		if (this.simStep % this.toChange == 0) {
			// Integer rate = this.dist.sample();
			Integer rate = this.rates[this.rIdx];
			System.out.println("new Rate=" + rate);
			this.roi.add(30.0 / (rate.doubleValue() / 1000.0));
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
			if (this.rIdx > this.rates.length) {
				try {
					Process proc = Runtime.getRuntime().exec("pkill -9 -f Workload-0.0.1-jar-with-dependencies.jar");
					proc.waitFor();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
