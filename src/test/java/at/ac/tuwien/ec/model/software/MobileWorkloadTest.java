package at.ac.tuwien.ec.model.software;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.ec.model.software.mobileapps.AntivirusApp;
import at.ac.tuwien.ec.model.software.mobileapps.ChessApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import at.ac.tuwien.ec.model.software.mobileapps.FacerecognizerApp;
import at.ac.tuwien.ec.model.software.mobileapps.NavigatorApp;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.*;

class MobileWorkloadTest {

	@Test
	void testMobileWorkload() {
		MobileWorkload mw = new MobileWorkload();
		Assert.assertNotNull(mw.componentList);
		Assert.assertNotNull(mw.taskDependencies);
	}

	@Test
	void testMobileWorkloadArrayListOfMobileApplication() {
		ArrayList<MobileApplication> wload = new ArrayList<MobileApplication>();
		wload.add(new FacebookApp(0, "mId"));
		MobileWorkload mwload = new MobileWorkload(wload);
		Assert.assertEquals(wload, mwload.getWorkload());
		Assert.assertNotNull(mwload.componentList);
		Assert.assertNotNull(mwload.taskDependencies);		
	}

	@Test
	void testJoinParallel() {
		MobileApplication fbApp = new FacebookApp(0,"mobile_0");
		MobileApplication fbApp2 = new FacebookApp(1,"mobile_0");
		MobileWorkload wLoad = new MobileWorkload();
		wLoad.joinParallel(fbApp);
		wLoad.joinParallel(fbApp2);
		ArrayList<MobileSoftwareComponent> mscCmp = new ArrayList<MobileSoftwareComponent>();
		ArrayList<ComponentLink> edges = new ArrayList<ComponentLink>();
		
		mscCmp.addAll(fbApp.taskDependencies.vertexSet());
		mscCmp.addAll(fbApp2.taskDependencies.vertexSet());
		edges.addAll(fbApp.taskDependencies.edgeSet());
		edges.addAll(fbApp2.taskDependencies.edgeSet());
		
		ArrayList<MobileSoftwareComponent> wLoadVertexSet = new ArrayList<MobileSoftwareComponent>(wLoad.getTaskDependencies().vertexSet());
		ArrayList<ComponentLink> wLoadEdgeSet = new ArrayList<ComponentLink>(wLoad.getTaskDependencies().edgeSet());
		Assert.assertTrue(wLoadVertexSet.containsAll(mscCmp));
		Assert.assertTrue(wLoadEdgeSet.containsAll(edges));
	}

	@Test
	void testJoinSequentially() {
		MobileApplication fbApp = new FacebookApp(0,"mobile_0");
		MobileApplication fbApp2 = new FacebookApp(1,"mobile_0");
		MobileWorkload wLoad = new MobileWorkload();
		wLoad.joinSequentially(fbApp);
		wLoad.joinSequentially(fbApp2);
		ArrayList<MobileSoftwareComponent> mscCmp = new ArrayList<MobileSoftwareComponent>();
		ArrayList<ComponentLink> edges = new ArrayList<ComponentLink>();
		
		mscCmp.addAll(fbApp.taskDependencies.vertexSet());
		mscCmp.addAll(fbApp2.taskDependencies.vertexSet());
		edges.addAll(fbApp.taskDependencies.edgeSet());
		edges.addAll(fbApp2.taskDependencies.edgeSet());
		
		ArrayList<MobileSoftwareComponent> wLoadVertexSet = new ArrayList<MobileSoftwareComponent>(wLoad.getTaskDependencies().vertexSet());
		ArrayList<ComponentLink> wLoadEdgeSet = new ArrayList<ComponentLink>(wLoad.getTaskDependencies().edgeSet());
		Assert.assertTrue(wLoadVertexSet.containsAll(mscCmp));
		Assert.assertTrue(wLoadEdgeSet.containsAll(edges));
		
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toWLoadIterator 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(wLoad.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp1Iterator 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(fbApp.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp2Iterator 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(fbApp2.getTaskDependencies());
				
		
		MobileSoftwareComponent currMsc;
		while(toWLoadIterator.hasNext() && toApp1Iterator.hasNext())
		{
			currMsc = toWLoadIterator.next();
			Assert.assertTrue(currMsc.equals(toApp1Iterator.next()));
		}
		while(toWLoadIterator.hasNext() && toApp2Iterator.hasNext())
		{
			currMsc = toWLoadIterator.next();
			Assert.assertTrue(currMsc.equals(toApp2Iterator.next()));
		}
				
	}
	
	@Test
	void testJoinSequentiallyAllApps()
	{
		MobileApplication app0 = new FacebookApp(0,"mobile_0");
		MobileApplication app1 = new FacerecognizerApp(1,"mobile_0");
		MobileApplication app2 = new ChessApp(2,"mobile_0");
		MobileApplication app3 = new NavigatorApp(3,"mobile_0");
		MobileApplication app4 = new AntivirusApp(4,"mobile_0");
		
		MobileWorkload wload = new MobileWorkload();
		wload.joinSequentially(app0);
		wload.joinSequentially(app1);
		wload.joinSequentially(app2);
		wload.joinSequentially(app3);
		wload.joinSequentially(app4);
		
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toWLoad
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(wload.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp0 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(app0.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp1 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(app1.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp2 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(app2.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp3 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(app3.getTaskDependencies());
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> toApp4 
		= new TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink>(app4.getTaskDependencies());
				
		
		MobileSoftwareComponent currMsc;
		while(toWLoad.hasNext() && toApp0.hasNext())
		{
			currMsc = toWLoad.next();
			Assert.assertTrue(currMsc.equals(toApp0.next()));
		}
		while(toWLoad.hasNext() && toApp1.hasNext())
		{
			currMsc = toWLoad.next();
			Assert.assertTrue(currMsc.equals(toApp1.next()));
		}
		while(toWLoad.hasNext() && toApp2.hasNext())
		{
			currMsc = toWLoad.next();
			Assert.assertTrue(currMsc.equals(toApp2.next()));
		}
		while(toWLoad.hasNext() && toApp3.hasNext())
		{
			currMsc = toWLoad.next();
			Assert.assertTrue(currMsc.equals(toApp3.next()));
		}
		while(toWLoad.hasNext() && toApp4.hasNext())
		{
			currMsc = toWLoad.next();
			Assert.assertTrue(currMsc.equals(toApp4.next()));
		}
		
	}

}
