package at.ac.tuwien.ec.model.software;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public abstract class MobileApplication implements Serializable, Cloneable{

	private int workloadId;
	private String userId;
	public DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> taskDependencies;
	public LinkedHashMap<String,MobileSoftwareComponent> componentList = new LinkedHashMap<String,MobileSoftwareComponent>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MobileApplication()
	{
		workloadId = 0;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new LinkedHashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}

	public MobileApplication(int wId)
	{
		workloadId = wId;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new LinkedHashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}
	
	public MobileApplication(int wId,String uId)
	{
		workloadId = wId;
		userId = uId;
		taskDependencies = new DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink>(ComponentLink.class);
		componentList = new LinkedHashMap<String,MobileSoftwareComponent>();
		setupTasks();
		setupLinks();
	}
	
	public int getWorkloadId() {
		return workloadId;
	}

	public void setWorkloadId(int workloadId) {
		this.workloadId = workloadId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void addComponent(String id, Hardware reqs, String uid)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,0.0, uid, 0,0);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, 0,0);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, int inData, int outData)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, boolean offloadable)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, 0,0, offloadable);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, double inData, double outData)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData, true);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
	
	public void addComponent(String id, Hardware reqs, String uid, double mips, double inData, double outData, boolean offloadable)
	{
		MobileSoftwareComponent cmp = new MobileSoftwareComponent(id,reqs,mips, uid, inData, outData, offloadable);
		componentList.put(cmp.getId(),cmp);
		taskDependencies.addVertex(cmp);
	}
		
	public ArrayList<MobileSoftwareComponent> getPredecessors(MobileSoftwareComponent msc)
	{
		ArrayList<MobileSoftwareComponent> preds = new ArrayList<MobileSoftwareComponent>();
		if(taskDependencies.containsVertex(msc))
			for(MobileSoftwareComponent p : Graphs.predecessorListOf(taskDependencies, msc))
				if(!preds.contains(p))
					preds.add(p);
		return preds;
	}
		
	
	public ArrayList<MobileSoftwareComponent> getNeighbors(MobileSoftwareComponent msc)
	{
		ArrayList<MobileSoftwareComponent> succs = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent s : Graphs.successorListOf(taskDependencies, msc))
			if(!succs.contains(s))
				succs.add(s);
		return succs;
	}
	
	public void addLink(String u, String v, QoSProfile requirements)
	{
		taskDependencies.addEdge(getComponentById(u), getComponentById(v), new ComponentLink(requirements));
	}
	
	public void addLink(MobileSoftwareComponent u, MobileSoftwareComponent v, QoSProfile requirements)
	{
		taskDependencies.addEdge(u, v, new ComponentLink(requirements));
	}
	
	public void addLink(String u, String v, double latency, double bandwidth)
	{
		if(!u.equals(v))
			taskDependencies.addEdge(getComponentById(u), getComponentById(v), new ComponentLink(new QoSProfile(latency, bandwidth)));
	}
	
	public void addLink(MobileSoftwareComponent u, MobileSoftwareComponent v, double latency, double bandwidth)
	{
		if(!u.equals(v))
			taskDependencies.addEdge(u, v, new ComponentLink(new QoSProfile(latency, bandwidth)));
	}
	
	public MobileSoftwareComponent getComponentById(String id){
		return componentList.get(id);
	}
	
	public double sampleLatency(){
		return ((SimulationSetup.lambdaLatency == 0)?
				Double.MAX_VALUE : 
					SimulationSetup.lambdaLatency 
					+ new ExponentialDistribution(SimulationSetup.lambdaLatency).sample()) ;
	}
	
	public boolean hasDependency(MobileSoftwareComponent c, MobileSoftwareComponent s) {
		return taskDependencies.containsEdge(c,s);
	}

	public boolean hasDependency(SoftwareComponent c, SoftwareComponent s) {
		return taskDependencies.containsEdge((MobileSoftwareComponent)c, (MobileSoftwareComponent) s);
	}
	
	public ComponentLink getDependency(MobileSoftwareComponent c, MobileSoftwareComponent s) {
		return taskDependencies.getEdge(c, s);
	}
	
	public ArrayList<ComponentLink> getIncomingEdgesIn(MobileSoftwareComponent msc)
	{
		ArrayList<ComponentLink> incomingEdges = new ArrayList<ComponentLink>();
		incomingEdges.addAll(taskDependencies.incomingEdgesOf(msc));
		return incomingEdges;
	}
	
	public ArrayList<ComponentLink> getOutgoingEdgesFrom(MobileSoftwareComponent msc)
	{
		ArrayList<ComponentLink> outgoingEdges = new ArrayList<ComponentLink>();
		outgoingEdges.addAll(taskDependencies.outgoingEdgesOf(msc));
		return outgoingEdges;
	}
	
	public void removeEdgesFrom(MobileSoftwareComponent msc)
	{
		ArrayList<ComponentLink> outgoing = new ArrayList<ComponentLink>();
		outgoing.addAll(taskDependencies.outgoingEdgesOf(msc));
		taskDependencies.removeAllEdges(outgoing);
	}
	
	public ArrayList<MobileSoftwareComponent> getTasks()
	{
		ArrayList<MobileSoftwareComponent> comps = new ArrayList<MobileSoftwareComponent>();
		for(Entry<String,MobileSoftwareComponent> e : componentList.entrySet())
			comps.add(e.getValue());
		
		return comps;
	}	
	
	public MobileSoftwareComponent getTaskByIndex(int num)
	{
		return getTasks().get(num);
	}
	
	public int getTaskIndex(MobileSoftwareComponent msc)
	{
		return getTasks().indexOf(msc);
	}
	
	public ArrayList<MobileSoftwareComponent> readyTasks()
	{
		ArrayList<MobileSoftwareComponent> readyTasks = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : taskDependencies.vertexSet())
			if(taskDependencies.incomingEdgesOf(msc).isEmpty())
				readyTasks.add(msc);
		return readyTasks;				
	}
	
	public int getComponentNum() {
		return componentList.size();
	}
	
	public void removeTask(MobileSoftwareComponent firstTaskToTerminate) {
		taskDependencies.removeVertex(firstTaskToTerminate);
		
	}
	
	public String toString()
	{
		return "mobileapp";
	}
	
	public MobileApplication clone() throws CloneNotSupportedException
	{
		return (MobileApplication) super.clone();
	}
	
	public DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> getTaskDependencies() {
		return taskDependencies;
	}
		
	
	public abstract void sampleTasks();
	
	public abstract void sampleLinks();
	
	public abstract void setupTasks();
	
	public abstract void setupLinks();

	
		

}
