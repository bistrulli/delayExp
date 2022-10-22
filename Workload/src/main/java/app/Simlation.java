package app;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import Server.SimpleTask;

public class Simlation extends Thread {
	
	SimpleTask generator=null;
	Integer simStep=null;
	Integer toChange=null;
	UniformIntegerDistribution dist=null;
	
	public Simlation(SimpleTask generator,Integer toChange) {
		this.generator=generator;
		this.simStep=0;
		this.toChange=toChange;
		this.dist=new UniformIntegerDistribution(50, 1000);
	}
	
	@Override
	public synchronized void start() {
		super.start();
		this.simStep+=1;
		if(this.simStep%this.toChange==0) {
			Integer rate = this.dist.sample();
			//devo pensare un modo per aggioranare dinamicamente il think rate dei clients
			
		}
	}
}
