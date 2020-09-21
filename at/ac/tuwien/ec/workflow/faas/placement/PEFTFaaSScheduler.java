package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import scala.Tuple2;

public class PEFTFaaSScheduler extends FaaSPlacementAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1153273992020302324L;
	double[][] OCT;
	private ArrayList<IoTDevice> publisherDevices;
	private ArrayList<MobileDevice> subscriberDevices;
	
	public PEFTFaaSScheduler(FaaSWorkflow A, MobileDataDistributionInfrastructure I)
	{
		super();
		setCurrentWorkflow(A);
		setInfrastructure(I);
		
		OCT = new double[A.getTasks().size()][I.getAllNodes().size()];
				
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		ArrayList<String> activeTopics = new ArrayList<String>();
		for(String topic : currInf.getRegistry().keySet())
		{
			if(!currInf.getRegistry().get(topic).isEmpty())
				activeTopics.add(topic);
		}
		String[] sourceTopics = activeTopics.toArray(new String[0]);
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		
		for(IoTDevice iot : currInf.getIotDevices().values())
		{
			Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
			iotTopics.retainAll(srcTopicSet);
			if(!iotTopics.isEmpty())
				publisherDevices.add(iot);
		}
		
		for(String t : activeTopics)
		{
			ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
	}
	
	public PEFTFaaSScheduler(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setCurrentWorkflow(arg._1());
		setInfrastructure(arg._2());
		OCT = new double[this.getCurrentWorkflow().getTasks().size()][this.getInfrastructure().getAllNodes().size()];
		
		/*for(MobileSoftwareComponent msc : currentApp.getTasks())
			{
			for(ComputationalNode node : currentInfrastructure.getAllNodes())
				System.out.print(OCT[currentApp.getTaskIndex(msc)][currentInfrastructure.getAllNodes().indexOf(node)] + ", ");
			System.out.println("");
			}*/

		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		ArrayList<String> activeTopics = new ArrayList<String>();
		for(String topic : currInf.getRegistry().keySet())
		{
			if(!currInf.getRegistry().get(topic).isEmpty())
				activeTopics.add(topic);
		}
		String[] sourceTopics = activeTopics.toArray(new String[0]);
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		
		for(IoTDevice iot : currInf.getIotDevices().values())
		{
			Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
			iotTopics.retainAll(srcTopicSet);
			if(!iotTopics.isEmpty())
				publisherDevices.add(iot);
		}
		
		for(String t : activeTopics)
		{
			ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		double startTime = System.currentTimeMillis();
		int currentTimestamp = 0;
		
		fillOCT(this.getCurrentWorkflow(),this.getInfrastructure());
		setRank(this.getCurrentWorkflow(),this.getInfrastructure());
		
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
	
		PriorityQueue<MobileSoftwareComponent> tasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		
		
		
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();

		DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> schedulingGraph 
			= (DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink>) this.getCurrentWorkflow().getTaskDependencies().clone();
		tasks.addAll(readyTasks(schedulingGraph));

		double currentRuntime = 0.0;
		MobileSoftwareComponent currTask;
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
		//ComputationalNode pred = (ComputationalNode) this.getInfrastructure().getNodeById("entry0"),target = null;
		ComputationalNode pred = null;
		ComputationalNode target = null;
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
			for(MobileSoftwareComponent cmp : this.getCurrentWorkflow().getPredecessors(currTask))
				if(cmp.getRunTime()>maxP) 
				{
					maxP = cmp.getRunTime();
					pred = (ComputationalNode) scheduling.get(cmp);
				}
			if(currTask.isOffloadable()) 
			{
				for(ComputationalNode cn : this.getInfrastructure().getAllNodes())
				{
					if(Double.isFinite(this.getInfrastructure().getTransmissionTime(currTask, pred, cn)))
					{
						if(maxP + ((pred==null)? currTask.getLocalRuntimeOnNode(cn, this.getInfrastructure()) : currTask.getRuntimeOnNode(pred, cn,this.getInfrastructure())) +
								OCT[this.getCurrentWorkflow().getTaskIndex(currTask)][this.getInfrastructure().getAllNodes().indexOf(cn)] < OeftMin)
						{
							tMin = maxP + ((pred==null)? currTask.getLocalRuntimeOnNode(cn, this.getInfrastructure()) : currTask.getRuntimeOnNode(pred, cn,this.getInfrastructure()));
							OeftMin =  tMin +
									OCT[this.getCurrentWorkflow().getTaskIndex(currTask)][this.getInfrastructure().getAllNodes().indexOf(cn)] ;
							target = cn;
						}
					}
				}
			}
			else 
			{
				for(ComputationalNode cn : this.getInfrastructure().getCloudNodes().values())
				{
					if(Double.isFinite(this.getInfrastructure().getTransmissionTime(currTask, pred, cn))) 
					{
						if(maxP + currTask.getRuntimeOnNode(pred, cn,this.getInfrastructure()) +
								OCT[this.getCurrentWorkflow().getTaskIndex(currTask)][this.getInfrastructure().getAllNodes().indexOf(cn)] < OeftMin)
						{
							tMin = maxP + currTask.getRuntimeOnNode(pred, cn, this.getInfrastructure());
							OeftMin =  tMin +
									OCT[this.getCurrentWorkflow().getTaskIndex(currTask)][this.getInfrastructure().getAllNodes().indexOf(cn)] ;
							target = cn;
						}
					}
				}
			}
			currentRuntime = tMin;
		
			if(target != null)
			{
				currTask.setRunTime(currentRuntime);
				deploy(scheduling,currTask,target,publisherDevices,subscriberDevices);
				
				currentTimestamp = (int) Math.round(getCurrentTime());
				for(MobileDevice d : this.getInfrastructure().getMobileDevices().values())
					d.updateCoordsWithMobility((double)currentTimestamp);
				MobilityBasedNetworkPlanner.setupMobileConnections(getInfrastructure());
				MobilityBasedNetworkPlanner.setupMobileConnections(getInfrastructure());
				MobileDevicePlannerWithMobility.updateDeviceSubscriptions(getInfrastructure(),
						SimulationSetup.selectedWorkflow);
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

		}
		double endTime = System.currentTimeMillis();
		scheduling.setExecutionTime(endTime - startTime);
		schedulings.add(scheduling);
		return schedulings;
	}
	
		
	private void setRank(FaaSWorkflow A, MobileCloudInfrastructure I)
	{
		double sum = 0.0;
		for(MobileSoftwareComponent msc : A.getTasks())
		{
			for(ComputationalNode node : I.getAllNodes())
				sum += OCT[A.getTaskIndex(msc)][I.getAllNodes().indexOf(node)];
			msc.setRank(sum/I.getAllNodes().size());
		}
	}
	
	private void fillOCT(FaaSWorkflow A, MobileCloudInfrastructure I) 
	{
		for(int j = 0; j < I.getAllNodes().size(); j++)
			OCT[A.getTasks().size()-1][j] = 0.0;
		
		for(int i = A.getTasks().size() - 2; i >= 0; i--)
			for(int j = 0; j < I.getAllNodes().size(); j++)
				computeOCTRank(i,j);
	}

	private void computeOCTRank(int i, int j) 
	{
		MobileSoftwareComponent ti = this.getCurrentWorkflow().getTaskByIndex(i);
		
		double maxOCTperTJ = 0.0;
		
		//finding average communication time
		double avgComm = 0.0;
		for(ComponentLink neigh : this.getCurrentWorkflow().getOutgoingEdgesFrom(ti)) 
		{
			for(ComputationalNode cn0 : this.getInfrastructure().getAllNodes())
				for(ComputationalNode cn1 : this.getInfrastructure().getAllNodes()) 
				{
					if(this.getInfrastructure().getConnectionMap().containsEdge(cn0,cn1))
					{
						double comm = this.getInfrastructure().getConnectionMap().getDataTransmissionTime(neigh.getTarget().getInData(),cn0, cn1);
						if(Double.isFinite(comm))
							avgComm += comm;
						if(Double.isInfinite(avgComm))
							System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
					}
				}
			avgComm = avgComm / this.getInfrastructure().getAllNodes().size();
			if(Double.isInfinite(avgComm))
				System.out.println("BBBBBBBBBBBBBBBBBBBBBBBB");
			//avgComm = avgComm / this.getCurrentWorkflow().getOutgoingEdgesFrom(ti).size();
		}
		
		for(MobileSoftwareComponent tj : this.getCurrentWorkflow().getNeighbors(ti)) 
		{
			double minOCTperPW = Double.MAX_VALUE;
						
			for(int k = 0; k < this.getInfrastructure().getAllNodes().size(); k++)
			{
				if(this.getCurrentWorkflow().getTaskIndex(tj)==-1)
					System.out.println(tj.getId());
				double tmp = OCT[this.getCurrentWorkflow().getTaskIndex(tj)][k] 
						+ tj.getLocalRuntimeOnNode(this.getInfrastructure().getAllNodes().get(k),
								this.getInfrastructure())
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
	
}
