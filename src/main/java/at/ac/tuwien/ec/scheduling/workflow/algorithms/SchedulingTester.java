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
	
	
	//static int[] mapping = {3,6,4,6,5,1,5,2,7,1,4,1,7,2,3,3,6,6,4,4,3,1,5,2,5,2};
	static int[] mapping = {6,3,6,6,5,3,1,1,2,4,7,4,6,10,5,3,5,1,2,4,2,5,1,4,2,3};
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
		int index = 0;
		while(toi.hasNext())
		{
			MobileSoftwareComponent curr = toi.next();
			double currRuntime = 0.0;
			double maxPredecessorRuntime = 0.0;
			ComputationalNode target, pred = currentInfrastructure.getNodeById("entry0");
			target = currentInfrastructure.getNodeById("entry0");
			if(curr.getId().contains("SOURCE") || curr.getId().contains("SINK"))
			{
				
				for(MobileSoftwareComponent cmp : currentApp.getPredecessors(curr))
				{
					if(cmp.getRunTime() > maxPredecessorRuntime) 
					{
						maxPredecessorRuntime = cmp.getRunTime() ;
					}
				}
				currRuntime = maxPredecessorRuntime; 
			}
			else if(curr.getId().contains("BARRIER"))
			{
				for(MobileSoftwareComponent cmp : currentApp.getPredecessors(curr))
				{
					if(cmp.getRunTime() > maxPredecessorRuntime) 
					{
						maxPredecessorRuntime = cmp.getRunTime() ;
						target = scheduling.get(cmp);
					}
				}
				currRuntime = maxPredecessorRuntime;
			}
			else
			{
				int nodeIdx = mapping[index];

				if (nodeIdx <= 6)
					target = clnodes.get(nodeIdx - 1);
				else
					target = ednodes.get(nodeIdx - 7);
				index++;
				
				ArrayList<MobileSoftwareComponent> preds = currentApp.getPredecessors(curr);
				//calculate max predecessor runtime considering transmission time
				for(MobileSoftwareComponent cmp : preds) 
				{
					double tmp = cmp.getRunTime();
					if(tmp > currRuntime) 
					{ 
						currRuntime = tmp;
						pred = scheduling.get(cmp); 
					}
				}
				//System.out.println(pred.getId()+ "," + target.getId());
				currRuntime += curr.getRuntimeOnNode(pred, target, currentInfrastructure);
				
			}
			curr.setRunTime(currRuntime);
			System.out.println(curr.getId() + "\t" +  target.getId() + "\t" + target.getMipsPerCore()  + "\t" + currRuntime);
			deploy(scheduling,curr,target);
		}
		schedulings.add(scheduling);
		return schedulings;
	}

}
