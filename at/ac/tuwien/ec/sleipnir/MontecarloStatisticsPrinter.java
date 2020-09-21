package at.ac.tuwien.ec.sleipnir;


public class MontecarloStatisticsPrinter {
	
	//private static final String header = "Deployment, Montecarlo frequency, Heuristic Rank, Fog Hardware %, Runtime, Cost for user, Energy consumption, Energy cost, Mobile budget";
	private static final String header = "# Runs\tDeployment\tMontecarlo-frequency\tRuntime\tUser-cost\tBattery-budget\tProvider-cost\tFog-nodes\tRuntime-l\tRuntime-h\tCost-l\tCost-h\tBattery-l\tBattery-h";
	private static final String separator = "\t";
	
	public static String getHeader(){
		return header;
	}
	
	/*public static String getStatistics(int runs, Deployment deployment, MobileCloudInfrastructure I, DeploymentsHistogram histogram, int times){
		return runs + separator +
				deployment.toString() 
				//"#"
				+ separator +
				toPercentage(histogram.getFrequency(deployment),times) + separator +
				//average(histogram.getScore(deployment),times) + ";" +
				//deployment.consumedResources.getA() + ";" +
				//deployment.consumedResources.getB() + separator +
				//(deployment.consumedResources.getA() + deployment.consumedResources.getB()) / 2.0 + separator +
				histogram.getAverageRuntime(deployment) + separator +
				histogram.getAverageCost(deployment) + separator +
				//deployment.energyConsumption + separator +
				//deployment.energyCost + separator +
				((histogram.getAverageBattery(deployment)/SimulationConstants.batteryCapacity)*100.0) + separator +
				histogram.getAverageProviderCost(deployment) + separator +
				I.F.size() + separator +
				histogram.getRuntimeConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[0] + separator + histogram.getRuntimeConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[1]  + separator +
				histogram.getCostConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[0] + separator + histogram.getCostConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[1] + separator +
				((histogram.getBatteryConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[0]/SimulationConstants.batteryCapacity)*100.0)
				+ separator 
				+ ((histogram.getBatteryConfidenceInterval(deployment, SimulationConstants.confidenceLevel)[1]/SimulationConstants.batteryCapacity)*100.0) + "\n";
	}*/
	
	private static double toPercentage(double n, int times){
		return 100.0 * n/times;
	}
	
	private static double average(double n, int times){
		return n/times;
	}
	
}
