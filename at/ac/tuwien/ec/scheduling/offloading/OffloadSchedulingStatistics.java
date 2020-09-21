package at.ac.tuwien.ec.scheduling.offloading;

import java.util.ArrayList;

import at.ac.tuwien.ec.scheduling.SchedulingStatistics;

public class OffloadSchedulingStatistics extends SchedulingStatistics{
	private ArrayList<Double> runtime,cost,battery,providerCost;

	public OffloadSchedulingStatistics(){
		runtime = new ArrayList<Double>();
		cost = new ArrayList<Double>();
		battery = new ArrayList<Double>();
		providerCost = new ArrayList<Double>();
	}
	
	public ArrayList<Double> getRuntime() {
		return runtime;
	}

	public void addRuntime(double runtime) {
		this.runtime.add(runtime);
	}

	public ArrayList<Double> getCost() {
		return cost;
	}

	public void addCost(double cost) {
		this.cost.add(cost);
	}

	public ArrayList<Double> getBattery() {
		return battery;
	}

	public void addBattery(double battery) {
		this.battery.add(battery);
	}
	
	public ArrayList<Double> getProviderCost() {
		return providerCost;
	}
	
	public void addProviderCost(double providerCost) {
		this.providerCost.add(providerCost);
	}
	
	public double calculateMean(ArrayList<Double> sample){
		Double tmp = 0.0;
		for(Double d: sample)
			tmp += d;
		return tmp / sample.size();
	}
	
	public double calculateVariance(ArrayList<Double> sample){
		Double tmp = 0.0;
		double mean = calculateMean(sample);
		for(Double d:sample)
			tmp += (d-mean)*(d-mean);
		return tmp / sample.size();
	}
	
	public double calculateStandardDeviation(ArrayList<Double> sample){
		return Math.sqrt(calculateVariance(sample));
	}
	
	public double[] getConfidenceInterval(ArrayList<Double> sample, double confidenceLevel){
		double[] interval = new double[2];
		double mean = calculateMean(sample);
		double stDv = calculateStandardDeviation(sample);
		double cfVal = (confidenceLevel * stDv) / Math.sqrt(sample.size()); 
		interval[0] = mean - cfVal;
		interval[1] = mean + cfVal;
		return interval;
	}
}
