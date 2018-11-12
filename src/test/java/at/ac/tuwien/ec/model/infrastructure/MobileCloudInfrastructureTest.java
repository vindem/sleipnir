package at.ac.tuwien.ec.model.infrastructure;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class MobileCloudInfrastructureTest {

	@Test
	public void testMobileCloudInfrastructure() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		Assert.assertTrue(inf instanceof MobileCloudInfrastructure);
		Assert.assertNotNull(inf.getCloudNodes());
		Assert.assertTrue(inf.getCloudNodes().size()==0);
		Assert.assertNotNull(inf.getEdgeNodes());
		Assert.assertTrue(inf.getEdgeNodes().size()==0);
		Assert.assertNotNull(inf.getMobileDevices());
		Assert.assertTrue(inf.getMobileDevices().size()==0);
	}

	@Test
	public void testAddMobileDevice() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		Assert.assertTrue(inf.getMobileDevices().size()==1);
		Assert.assertSame(md, inf.getMobileDevices().get(0));
	}

	@Test
	public void testAddEdgeNode() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		inf.addEdgeNode(en);
		Assert.assertTrue(inf.getEdgeNodes().size()==1);
		Assert.assertSame(en, inf.getEdgeNodes().get(0));
	}

	@Test
	public void testAddCloudDataCenter() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		CloudDataCenter cdc = new CloudDataCenter("test",
				SimulationSetup.defaultCloudCapabilities);
		Assert.assertNotNull(cdc);
		inf.addCloudDataCenter(cdc);
		Assert.assertTrue(inf.getEdgeNodes().size()==1);
		Assert.assertSame(cdc, inf.getCloudNodes().get(0));
	}

	@Test
	public void testAddLinkComputationalNodeComputationalNodeQoSProfile() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddLinkComputationalNodeComputationalNodeDoubleDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testSampleInfrastructure() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMobileDevices() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetEdgeNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCloudNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddPrices() {
		fail("Not yet implemented");
	}

}
