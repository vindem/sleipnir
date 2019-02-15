package at.ac.tuwien.ec.scheduling.workflow.algorithms;

import java.util.ArrayList;
import java.util.Comparator;

import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;
import scala.Tuple2;

public class SchedulingTester extends WorkflowScheduler {
	
	//static int[] mapping = {3,5,6,5,1,1,2,8,6,8,8,5,8,4,6,3,1,3,4,2,6,4,4,3,1,5,2};
	//static int[] mapping = {6,6,1,2,4,1,7,10,7,3,10,3,7,7,4,4,1,3,6,1,3,2,5,5,5,5,2};
	static int[] mapping = {1,2,5,6,2,4,6,4,1,3,5,7,7,5,1,2,6,6,3,5,4,4,2,1,3,3};
	public SchedulingTester(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		
	}

	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<WorkflowScheduling> schedulings = new ArrayList<WorkflowScheduling>();
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toi = 
				new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(this.currentApp.getTaskDependencies());
		WorkflowScheduling scheduling = new WorkflowScheduling();
		int index = 0;
		
		ArrayList<CloudDataCenter> clnodes = new ArrayList<CloudDataCenter>(currentInfrastructure.getCloudNodes().values());
		ArrayList<EdgeNode> ednodes = new ArrayList<EdgeNode>(currentInfrastructure.getEdgeNodes().values());
		clnodes.sort(
				new Comparator<CloudDataCenter>() {

					@Override
					public int compare(CloudDataCenter o1, CloudDataCenter o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
		
		ednodes.sort(
				new Comparator<EdgeNode>(){

					@Override
					public int compare(EdgeNode o1, EdgeNode o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
		while(toi.hasNext())
		{
			MobileSoftwareComponent curr = toi.next();
			double tMin = Double.MAX_VALUE;
			ComputationalNode pred = null;
			double maxP = 0.0;
			ComputationalNode target;
			for(MobileSoftwareComponent cmp : currentApp.getPredecessors(curr))
				if(cmp.getRunTime()>maxP) 
				{
					maxP = cmp.getRunTime();
					pred = scheduling.get(cmp);
				}
			if(curr.getId().contains("BARRIER") || curr.getId().contains("SOURCE") || curr.getId().contains("SINK"))
			{
				target = currentInfrastructure.getNodeById("entry0");
				curr.setRunTime(maxP);
			}
			else
			{
				int nodeIdx = mapping[index];
				
				if (nodeIdx <= 6)
					target = clnodes.get(nodeIdx - 1);
				else
					target = ednodes.get(nodeIdx - 7);
				
				tMin = maxP + curr.getRuntimeOnNode(pred, target, currentInfrastructure);
				curr.setRunTime(tMin);
				index++;
			}
			deploy(scheduling,curr,target);
				
			
			
		}
		schedulings.add(scheduling);
		return schedulings;
	}

}
