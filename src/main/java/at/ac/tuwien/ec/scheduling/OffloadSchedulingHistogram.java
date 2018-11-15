package at.ac.tuwien.ec.scheduling;

import java.util.HashMap;
import java.util.List;


public class OffloadSchedulingHistogram extends HashMap<OffloadScheduling, OffloadSchedulingStatistics>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3605212001840784279L;
	private String algorithmName;
	
	public OffloadSchedulingHistogram(){
		super();
	}
	
	public OffloadSchedulingHistogram(String algorithmName){
		super();
		this.algorithmName = algorithmName;
	}
	
	public void add(OffloadScheduling d, double score)
	{
		OffloadSchedulingStatistics stats = new OffloadSchedulingStatistics();
		stats.setFrequency(1.0);
		stats.setScore(score);
		stats.addRuntime(d.getRunTime());
		stats.addBattery(d.getBatteryLifetime());
		stats.addCost(d.getUserCost());
		stats.addProviderCost(d.getProviderCost());
		super.put(d,stats);
	}
	
	public void add(OffloadScheduling d, double freq, double score){
		OffloadSchedulingStatistics stats = new OffloadSchedulingStatistics();
		stats.setFrequency(freq);
		stats.setScore(score);
		stats.addRuntime(d.getRunTime());
		stats.addBattery(d.getBatteryLifetime());
		stats.addCost(d.getUserCost());
		stats.addProviderCost(d.getProviderCost());
		super.put(d, stats);
	}
	
	public void update(OffloadScheduling d,double score){
		if(super.containsKey(d))
		{
			OffloadSchedulingStatistics tmp = super.get(d);
			tmp.setFrequency(tmp.getFrequency() + 1.0);
			tmp.addBattery(d.getBatteryLifetime());
			tmp.addRuntime(d.getRunTime());
			tmp.addCost(d.getUserCost());
			tmp.addProviderCost(d.getProviderCost());
			super.replace(d, tmp);
		}
	}

	public Double getFrequency(OffloadScheduling deployment) {
		return super.get(deployment).getFrequency();
	}
	
	public Double getScore(OffloadScheduling deployment){
		return super.get(deployment).getScore();
	}

	public double getAverageBattery(OffloadScheduling deployment) {
		Double tmp = 0.0;
		for(Double d : super.get(deployment).getBattery())
			tmp += d;
		return (tmp.doubleValue() / super.get(deployment).getFrequency());
	}

	public double getAverageRuntime(OffloadScheduling deployment) {
		Double tmp = 0.0;
		for(Double d: super.get(deployment).getRuntime())
			tmp += d;
		return (tmp.doubleValue() / super.get(deployment).getFrequency());
	}

	public double getAverageCost(OffloadScheduling deployment) {
		double tmp = 0;
		for(Double d : super.get(deployment).getCost())
			tmp += d;
		return tmp / super.get(deployment).getFrequency();
	}
	
	public double getAverageProviderCost(OffloadScheduling deployment) {
		double tmp = 0.0;
		for(Double d : super.get(deployment).getProviderCost())
			tmp += d;
		return (tmp / super.get(deployment).getFrequency());
	}
	
	public double[] getRuntimeConfidenceInterval(OffloadScheduling deployment, double confidenceLevel){
		return super.get(deployment).getConfidenceInterval(super.get(deployment).getRuntime(), confidenceLevel);
	}
	
	public double[] getCostConfidenceInterval(OffloadScheduling deployment, double confidenceLevel){
		return super.get(deployment).getConfidenceInterval(super.get(deployment).getCost(), confidenceLevel);
	}
	
	public double[] getBatteryConfidenceInterval(OffloadScheduling deployment, double confidenceLevel){
		return super.get(deployment).getConfidenceInterval(super.get(deployment).getBattery(), confidenceLevel);
	}
	
}
