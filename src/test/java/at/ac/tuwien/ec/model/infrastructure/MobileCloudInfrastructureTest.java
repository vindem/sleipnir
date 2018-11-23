package at.ac.tuwien.ec.model.infrastructure;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
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
	public void testAddLinkComputationalNodeComputationalNodeQoSProfileAllValid() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("testEdge",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("testMobile",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		inf.addEdgeNode(en);
		QoSProfile prof = new QoSProfile(10.0,1.0);
		inf.addLink(md, en, prof);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeQoSProfileUNull() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = null;
		MobileDevice md = null;
		QoSProfile prof = new QoSProfile(10.0,1.0);
		inf.addLink(md, en, prof);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeQoSProfileUInvalid() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		inf.addEdgeNode(en);
		QoSProfile prof = new QoSProfile(10.0,1.0);
		inf.addLink(md, en, prof);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeQoSProfileProfileNotValid() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		QoSProfile prof = null;
		inf.addLink(md, en, prof);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeQoSProfileVNull() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = null;
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		QoSProfile prof = new QoSProfile(10.0,1.0);
		inf.addLink(md, en, prof);
	}

	@Test
	public void testAddLinkComputationalNodeComputationalNodeDoubleDoubleAllValid() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		inf.addEdgeNode(en);
		inf.addLink(md, en, 5.0,5.0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeDoubleDoubleInvalidLatency() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		inf.addEdgeNode(en);
		inf.addLink(md, en, -5.0,5.0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddLinkComputationalNodeComputationalNodeDoubleDoubleInvalidBandwidth() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		inf.addEdgeNode(en);
		inf.addLink(md, en, 5.0,-5.0);
	}

	@Test
	public void testGetTransmissionTimeAllFine(){
		//setting up link
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("testEdge",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		MobileDevice md = new MobileDevice("testMobile",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		Assert.assertNotNull(md);
		inf.addMobileDevice(md);
		inf.addEdgeNode(en);
		QoSProfile prof = new QoSProfile(10.0,1.0);
		inf.addLink(md, en, prof);
		MobileSoftwareComponent msc = new MobileSoftwareComponent("test_component",
				new Hardware(1, 1, 1), 50, "testMobile", 500, 1500);
		md.setCoords(new Coordinates(1.0,1.0));
		en.setCoords(new Coordinates(3.0,3.0));
		Assert.assertTrue(inf.getTransmissionTime(msc,md,en)>0);
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
