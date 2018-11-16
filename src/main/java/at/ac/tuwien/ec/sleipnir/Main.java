package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.edge.RandomEdgePlanner;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.heuristics.MinMinResearch;
import at.ac.tuwien.ec.scheduling.algorithms.multiobjective.RandomScheduler;

public class Main {
	
	public static void main(String[] arg)
	{
		MobileCloudInfrastructure infrastructure = new MobileCloudInfrastructure();
		DefaultCloudPlanner.setupCloudNodes(infrastructure, 4);
		EdgeAllCellPlanner.setupEdgeNodes(infrastructure);
		DefaultMobileDevicePlanner.setupMobileDevices(infrastructure, 1);
		DefaultNetworkPlanner.setupNetworkConnections(infrastructure);
		MobileApplication facebookApp = new FacebookApp(0,"mobile_0");
		RandomScheduler research = new RandomScheduler(facebookApp, infrastructure);
		ArrayList<OffloadScheduling> scheduling = research.findScheduling();
		System.out.println(scheduling.get(0));
	}

}
