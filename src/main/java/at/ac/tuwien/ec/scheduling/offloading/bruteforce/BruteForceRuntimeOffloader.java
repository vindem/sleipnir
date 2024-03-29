package at.ac.tuwien.ec.scheduling.offloading.bruteforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective.scheduling.DeploymentSolution;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import scala.Tuple2;

public class BruteForceRuntimeOffloader extends OffloadScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8141744080579698176L;
	ArrayList<BruteForceSolution> schedulings;
	private HashMap<MobileSoftwareComponent,Boolean> visitedList;

	public BruteForceRuntimeOffloader(MobileApplication app, MobileCloudInfrastructure I)
	{
		super();
		setMobileApplication(app);
		setInfrastructure(I);
		visitedList = new HashMap<MobileSoftwareComponent,Boolean>();
	}
	
	public BruteForceRuntimeOffloader(Tuple2<MobileApplication,MobileCloudInfrastructure> t)
	{
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		visitedList = new HashMap<MobileSoftwareComponent,Boolean>();
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		double start = System.nanoTime();
		ArrayList<MobileSoftwareComponent> taskList = new ArrayList<MobileSoftwareComponent>();
		DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> deps = this.getMobileApplication().getTaskDependencies();
		schedulings = new ArrayList<BruteForceSolution>();
		
		Iterator<MobileSoftwareComponent> it = deps.iterator();
		while(it.hasNext())
			taskList.add(it.next());
		
		BruteForceSolution current = new BruteForceSolution(currentApp,currentInfrastructure);
		
		Tree<ComputationalNode> combinations = new Tree<ComputationalNode>();
		Node<ComputationalNode> root = new Node<ComputationalNode>(currentInfrastructure.getMobileDevices().get(taskList.get(0).getUserId()));
		combinations.setRootElement(root);
		
		Node<ComputationalNode> currNode = combinations.getRootElement();
		
		combUtil(current,currNode,taskList,0,taskList.size());
	
		ArrayList<OffloadScheduling> toRet = new ArrayList<OffloadScheduling>();
		
		double minRt = Double.MAX_VALUE;
		OffloadScheduling target = null;
		
		
		for(BruteForceSolution bfs : schedulings)
		{
			OffloadScheduling os = bfs.evaluate(currentApp,currentInfrastructure);
			if(os.getRunTime() < minRt)
			{
				minRt = os.getRunTime();
				target = os;
			}
		}
		double end = System.nanoTime();
		if(target != null)
		{
			target.setExecutionTime(end - start);
			toRet.add(target);
		}
		else
		{
			OffloadScheduling os = current.evaluate(currentApp,currentInfrastructure);
			os.setExecutionTime(end - start);
			toRet.add(os);
		}
		System.out.println(toRet.get(0).getRunTime());
		return toRet;
	}

	
	

	private void combUtil(BruteForceSolution sch, Node<ComputationalNode> root, ArrayList<MobileSoftwareComponent> taskList, int currIndex, int size)
	{
		if(root == null)
			return;
		if(currIndex == size) 
		{
			sch.addTuple(taskList.get(currIndex-1),root.getData());
			schedulings.add(sch);
			return;
		}
		
		BruteForceSolution current = sch.copy();
		
		MobileSoftwareComponent msc = taskList.get(currIndex);
		
		Node<ComputationalNode> mobileNode = new Node<ComputationalNode>((ComputationalNode) currentInfrastructure.getNodeById(msc.getUserId()));
		
		
		if(!visitedList.containsKey(msc))
		{
			root.addChild(mobileNode);
			if(msc.isOffloadable())
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					root.addChild(new Node<ComputationalNode>(cn));
			visitedList.put(msc, true);
		}
		if(root.getData()!=null)
			current.addTuple(msc,root.getData());
		combUtil(current, root.getChildren(), taskList, currIndex + 1, taskList.size());
		
		if(root.getRightSibling()!=null)
			combUtil(current,root.getRightSibling(), taskList, currIndex + 1, taskList.size());
	}

	@Override
	public ComputationalNode findTarget(OffloadScheduling s, MobileSoftwareComponent msc) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
