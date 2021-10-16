package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics;

import java.util.ArrayList;
import java.util.PriorityQueue;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;
import scala.Tuple2;

public class PEFTOffloadScheduler extends OffloadScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1153273992020302324L;
	double[][] OCT;
	
	public PEFTOffloadScheduler(MobileApplication A, MobileCloudInfrastructure I)
	{
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		
		OCT = new double[A.getTasks().size()][I.getAllNodes().size()];
		fillOCT(A,I);
		setRank(A,I);
	}
	
	public PEFTOffloadScheduler(Tuple2<MobileApplication,MobileCloudInfrastructure> t)
	{
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		OCT = new double[currentApp.getTasks().size()][currentInfrastructure.getAllNodes().size()];
		fillOCT(currentApp,currentInfrastructure);
		setRank(currentApp,currentInfrastructure);
		/*for(MobileSoftwareComponent msc : currentApp.getTasks())
			{
			for(ComputationalNode node : currentInfrastructure.getAllNodes())
				System.out.print(OCT[currentApp.getTaskIndex(msc)][currentInfrastructure.getAllNodes().indexOf(node)] + ", ");
			System.out.println("");
			}*/
				
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());

		PriorityQueue<MobileSoftwareComponent> tasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());



		ArrayList<OffloadScheduling> deployments = new ArrayList<OffloadScheduling>();

		DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> schedulingGraph 
		= (DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink>) currentApp.getTaskDependencies().clone();
		tasks.addAll(readyTasks(schedulingGraph));

		double currentRuntime = 0.0;
		MobileSoftwareComponent currTask;
		OffloadScheduling scheduling = new OffloadScheduling();
		ComputationalNode pred = (ComputationalNode) currentInfrastructure.getNodeById("entry0"),target = null;
		while((currTask = tasks.poll())!=null)
		{
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = scheduledNodes.remove();
				if(currentRuntime < firstTaskToTerminate.getRunTime())
				{
					currentRuntime = firstTaskToTerminate.getRunTime();
					pred = (ComputationalNode) scheduling.get(firstTaskToTerminate);
				}
				((ComputationalNode) scheduling.get(firstTaskToTerminate)).undeploy(firstTaskToTerminate);				
			}
			double OeftMin = Double.MAX_VALUE, tMin = Double.MAX_VALUE;


			double maxP = 0.0;
			for(MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask))
				if(cmp.getRunTime()>maxP) 
				{
					maxP = cmp.getRunTime();
					//pred = scheduling.get(cmp);
				}					
			if(!currTask.isOffloadable())
			{
				if(isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
				{
					target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
					scheduledNodes.add(currTask);
				}
				else
				{
					if(scheduledNodes.isEmpty())
						target = null;
				}
			}
			else {
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(maxP + currTask.getRuntimeOnNode((ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()), cn,currentInfrastructure) +
							OCT[currentApp.getTaskIndex(currTask)][currentInfrastructure.getAllNodes().indexOf(cn)] < OeftMin &&
							isValid(scheduling,currTask,cn))
					{
						tMin = maxP + currTask.getRuntimeOnNode((ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()),
								cn,
								currentInfrastructure);
						OeftMin =  tMin +
								OCT[currentApp.getTaskIndex(currTask)][currentInfrastructure.getAllNodes().indexOf(cn)] ;
						target = cn;
					}
				if(maxP + currTask.getRuntimeOnNode((ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()), currentInfrastructure) < tMin
						&& isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
					target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
				currentRuntime = tMin;
			}
			if(target != null)
			{
				deploy(scheduling,currTask,target);
				scheduledNodes.add(currTask);
				if(schedulingGraph.containsVertex(currTask))
				{
					ArrayList<ComponentLink> outEdges = new ArrayList<ComponentLink>();
					outEdges.addAll(schedulingGraph.outgoingEdgesOf(currTask));
					schedulingGraph.removeAllEdges(outEdges);
					schedulingGraph.removeVertex(currTask);
				}
				tasks.addAll(readyTasks(schedulingGraph));
			}
			else
			{
				if(scheduledNodes.isEmpty())
					target = null;
			}

		}
		deployments.add(scheduling);
		return deployments;
	}
	
	private void setRank(MobileApplication A, MobileCloudInfrastructure I)
	{
		double sum = 0.0;
		for(MobileSoftwareComponent msc : A.getTasks())
		{
			for(ComputationalNode node : I.getAllNodes())
				sum += OCT[A.getTaskIndex(msc)][I.getAllNodes().indexOf(node)];
			msc.setRank(sum/(I.getAllNodes().size()+1));
		}
	}
	
	private void fillOCT(MobileApplication A, MobileCloudInfrastructure I) 
	{
		for(int j = 0; j < I.getAllNodes().size(); j++)
			OCT[A.getTasks().size()-1][j] = 0.0;
		
		for(int i = A.getTasks().size() - 2; i >= 0; i--)
			for(int j = 0; j < I.getAllNodes().size(); j++)
				computeOCTRank(i,j);
	}

	private void computeOCTRank(int i, int j) 
	{
		MobileSoftwareComponent ti = this.currentApp.getTaskByIndex(i);
		
		double maxOCTperTJ = 0.0;
		
		//finding average communication time
		double avgComm = 0.0;
		for(ComponentLink neigh : currentApp.getOutgoingEdgesFrom(ti)) 
		{
			for(ComputationalNode cn0 : currentInfrastructure.getMobileDevices().values())
				for(ComputationalNode cn1 : currentInfrastructure.getEdgeNodes().values())
					avgComm += currentInfrastructure.getTransmissionTime(neigh.getTarget(),cn0, cn1);
			
			for(ComputationalNode cn0 : currentInfrastructure.getMobileDevices().values())
				for(ComputationalNode cn1 : currentInfrastructure.getCloudNodes().values())
					avgComm += currentInfrastructure.getTransmissionTime(neigh.getTarget(),cn0, cn1);
			
			avgComm = avgComm / (currentInfrastructure.getCloudNodes().size() + currentInfrastructure.getEdgeNodes().size() + 1);
		}
		
		for(MobileSoftwareComponent tj : currentApp.getNeighbors(ti)) 
		{
			double minOCTperPW = Double.MAX_VALUE;
						
			for(int k = 0; k < currentInfrastructure.getAllNodes().size(); k++)
			{
				double tmp = OCT[currentApp.getTaskIndex(tj)][k] 
						+ tj.getLocalRuntimeOnNode(currentInfrastructure.getAllNodes().get(k), currentInfrastructure)
						+ avgComm;
				if(tmp < minOCTperPW)
					minOCTperPW = tmp;
			}
			if(minOCTperPW > maxOCTperTJ)
				maxOCTperTJ = minOCTperPW;
		}
		
		OCT[i][j] = maxOCTperTJ;
	}

	private ArrayList<MobileSoftwareComponent> readyTasks(DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> taskDependencies)
	{
		ArrayList<MobileSoftwareComponent> readyTasks = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : taskDependencies.vertexSet())
			if(taskDependencies.incomingEdgesOf(msc).isEmpty())
				readyTasks.add(msc);
		return readyTasks;		
	}

	@Override
	public ComputationalNode findTarget(OffloadScheduling s, MobileSoftwareComponent msc) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
