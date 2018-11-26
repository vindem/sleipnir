package at.ac.tuwien.ec.model.software;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.ec.model.software.mobileapps.FacebookApp;
import org.junit.*;

class MobileWorkloadTest {

	@Test
	void testMobileWorkload() {
		fail("Not yet implemented");
	}

	@Test
	void testMobileWorkloadArrayListOfMobileApplication() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

}
