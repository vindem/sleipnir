package at.ac.tuwien.ec.scheduling.workflow;

import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.ec.scheduling.SchedulingHistogram;


public class WorkflowSchedulingHistogram extends SchedulingHistogram{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3605212001840784279L;
	private String algorithmName;
	
	public WorkflowSchedulingHistogram(){
		super();
	}
	
	public WorkflowSchedulingHistogram(String algorithmName){
		super();
		this.algorithmName = algorithmName;
	}
	
	public void add(WorkflowScheduling d, double score)
	{
		WorkflowSchedulingStatistics stats = new WorkflowSchedulingStatistics();
		stats.setFrequency(1.0);
		stats.setScore(score);
		stats.addRuntime(d.getRunTime());
		stats.addCost(d.getUserCost());
		stats.addReliability(d.getReliability());
		super.put(d,stats);
	}
	
	public void add(WorkflowScheduling d, double freq, double score){
		WorkflowSchedulingStatistics stats = new WorkflowSchedulingStatistics();
		stats.setFrequency(freq);
		stats.setScore(score);
		stats.addRuntime(d.getRunTime());
		stats.addCost(d.getUserCost());
		stats.addReliability(d.getReliability());
		super.put(d, stats);
	}
	
	public void update(WorkflowScheduling d,double score){
		if(super.containsKey(d))
		{
			WorkflowSchedulingStatistics tmp = (WorkflowSchedulingStatistics) super.get(d);
			tmp.setFrequency(tmp.getFrequency() + 1.0);
			tmp.addRuntime(d.getRunTime());
			tmp.addCost(d.getUserCost());
			tmp.addReliability(d.getReliability());
			super.replace(d, tmp);
		}
	}

	public Double getFrequency(WorkflowScheduling deployment) {
		return super.get(deployment).getFrequency();
	}
	
	public Double getScore(WorkflowScheduling deployment){
		return super.get(deployment).getScore();
	}

	public double getAverageReliability(WorkflowScheduling deployment) {
		Double tmp = 0.0;
		for(Double d : ((WorkflowSchedulingStatistics) super.get(deployment)).getReliability())
			tmp += d;
		return (tmp.doubleValue() / super.get(deployment).getFrequency());
	}

	public double getAverageRuntime(WorkflowScheduling deployment) {
		Double tmp = 0.0;
		for(Double d: ((WorkflowSchedulingStatistics) super.get(deployment)).getRuntime())
			tmp += d;
		return (tmp.doubleValue() / super.get(deployment).getFrequency());
	}

	public double getAverageCost(WorkflowScheduling deployment) {
		double tmp = 0;
		for(Double d : ((WorkflowSchedulingStatistics) super.get(deployment)).getCost())
			tmp += d;
		return tmp / super.get(deployment).getFrequency();
	}
	
		
	public double[] getRuntimeConfidenceInterval(WorkflowScheduling deployment, double confidenceLevel){
		return ((WorkflowSchedulingStatistics) super.get(deployment)).getConfidenceInterval(((WorkflowSchedulingStatistics) super.get(deployment)).getRuntime(), confidenceLevel);
	}
	
	public double[] getCostConfidenceInterval(WorkflowScheduling deployment, double confidenceLevel){
		return ((WorkflowSchedulingStatistics) super.get(deployment)).getConfidenceInterval(((WorkflowSchedulingStatistics) super.get(deployment)).getCost(), confidenceLevel);
	}
	
	public double[] getReliabilityConfidenceInterval(WorkflowScheduling deployment, double confidenceLevel){
		return ((WorkflowSchedulingStatistics) super.get(deployment)).getConfidenceInterval(((WorkflowSchedulingStatistics) super.get(deployment)).getReliability(), confidenceLevel);
	}
	
}
