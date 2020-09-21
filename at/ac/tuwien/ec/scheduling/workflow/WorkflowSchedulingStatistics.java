package at.ac.tuwien.ec.scheduling.workflow;

import java.util.ArrayList;

import at.ac.tuwien.ec.scheduling.SchedulingStatistics;

public class WorkflowSchedulingStatistics extends SchedulingStatistics{
	private double frequency,score;
	private ArrayList<Double> runtime,reliability,usercost;

	public WorkflowSchedulingStatistics(){
		runtime = new ArrayList<Double>();
		reliability = new ArrayList<Double>();
		usercost = new ArrayList<Double>();
	}
	
	public Double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public ArrayList<Double> getRuntime() {
		return runtime;
	}

	public void addRuntime(double runtime) {
		this.runtime.add(runtime);
	}

	public ArrayList<Double> getCost() {
		return usercost;
	}

	public void addCost(double cost) {
		this.usercost.add(cost);
	}

	public ArrayList<Double> getReliability() {
		return reliability;
	}

	public void addReliability(double battery) {
		this.reliability.add(battery);
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
