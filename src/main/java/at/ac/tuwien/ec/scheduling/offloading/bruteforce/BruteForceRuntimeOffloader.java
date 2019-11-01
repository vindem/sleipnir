package at.ac.tuwien.ec.scheduling.offloading.bruteforce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import scala.Tuple2;

public class BruteForceRuntimeOffloader extends OffloadScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8141744080579698176L;
	ArrayList<OffloadScheduling> schedulings;

	public BruteForceRuntimeOffloader(MobileApplication app, MobileCloudInfrastructure I)
	{
		super();
		setMobileApplication(app);
		setInfrastructure(I);
	}
	
	public BruteForceRuntimeOffloader(Tuple2<MobileApplication,MobileCloudInfrastructure> t)
	{
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<MobileSoftwareComponent> taskList = new ArrayList<MobileSoftwareComponent>();
		DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> deps = this.getMobileApplication().getTaskDependencies();
		schedulings = new ArrayList<OffloadScheduling>();
		
		Iterator<MobileSoftwareComponent> it = deps.iterator();
		while(it.hasNext())
			taskList.add(it.next());
		
		OffloadScheduling current = new OffloadScheduling();
		
		Tree<ComputationalNode> combinations = new Tree<ComputationalNode>();
		Node<ComputationalNode> root = new Node<ComputationalNode>(null);
		combinations.setRootElement(root);
		
		Node<ComputationalNode> currNode = combinations.getRootElement();
		
		combUtil(current,currNode,taskList,0,taskList.size());
	
		ArrayList<OffloadScheduling> toRet = new ArrayList<OffloadScheduling>();
		
		double minRt = Double.MAX_VALUE;
		OffloadScheduling target = null;
		for(OffloadScheduling os : schedulings)
			if(os.getRunTime() < minRt)
			{
				minRt = os.getRunTime();
				target = os;
			}
		toRet.add(target);
		return toRet;
	}

	private void combUtil(OffloadScheduling sch, Node<ComputationalNode> root, ArrayList<MobileSoftwareComponent> taskList, int currIndex, int size)
	{
		if(currIndex == size) 
		{
			deploy(sch,taskList.get(currIndex-1),root.getData());
			schedulings.add(sch);
			return;
		}
		
		OffloadScheduling current = (OffloadScheduling) sch.clone();
		MobileSoftwareComponent msc = taskList.get(currIndex);
		//System.out.println(currIndex);
		Node<ComputationalNode> mobileNode = new Node<ComputationalNode>((ComputationalNode) currentInfrastructure.getNodeById(msc.getUserId()));
		
		//System.out.println(currIndex);
		root.addChild(mobileNode);
		if(msc.isOffloadable())
			for(ComputationalNode cn : currentInfrastructure.getAllNodes())
				root.addChild(new Node<ComputationalNode>(cn));
		if(root.getData()!=null)
			deploy(current,msc,root.getData());
		combUtil(current, root.getChildren(), taskList, currIndex + 1, taskList.size());
		if(root.getRightSibling()!=null)
		combUtil(current,root.getRightSibling(), taskList, currIndex + 1, taskList.size());
		
	}

	
	
}
