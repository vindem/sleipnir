package at.ac.tuwien.ec.model.infrastructure;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.QoSProfile;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.costs.ElectricityPriceTrace;
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
		ArrayList<MobileDevice> devsArray = new ArrayList<MobileDevice>(inf.getMobileDevices().values());
		Assert.assertTrue(devsArray.size()==1);
		Assert.assertTrue(devsArray.get(0).equals(md));
	}

	@Test
	public void testAddEdgeNode() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		Assert.assertNotNull(en);
		inf.addEdgeNode(en);
		ArrayList<EdgeNode> eNodes = new ArrayList<EdgeNode>(inf.getEdgeNodes().values());
		Assert.assertTrue(eNodes.size()==1);
		Assert.assertEquals(eNodes.get(0), en);
	}

	@Test
	public void testAddCloudDataCenter() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		CloudDataCenter cdc = new CloudDataCenter("test",
				SimulationSetup.defaultCloudCapabilities);
		Assert.assertNotNull(cdc);
		inf.addCloudDataCenter(cdc);
		ArrayList<CloudDataCenter> cNodes = new ArrayList<CloudDataCenter>(inf.getCloudNodes().values());
		Assert.assertTrue(cNodes.size()==1);
		Assert.assertEquals(cNodes.get(0),cdc);
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
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		ArrayList<ComputationalNode> nodes = inf.getAllNodes();
		Assert.assertTrue(nodes.size()==0);
		CloudDataCenter cdc = new CloudDataCenter("test",
				SimulationSetup.defaultCloudCapabilities);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		inf.addCloudDataCenter(cdc);
		inf.addEdgeNode(en);
		inf.sampleInfrastructure();
	}

	@Test
	public void testSetMobileDevices() {
		HashMap<String,MobileDevice> devs = new HashMap<String,MobileDevice>();
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertTrue(inf.getMobileDevices().size()==0);
		MobileDevice md = new MobileDevice("test",
				SimulationSetup.defaultMobileDeviceHardwareCapabilities,
				10.0);
		devs.put(md.getId(),md);
		inf.setMobileDevices(devs);
		Assert.assertTrue(inf.getMobileDevices().size()==devs.size());
		Assert.assertEquals(devs, inf.getMobileDevices());
	}

	@Test
	public void testSetEdgeNodes() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		HashMap<String,EdgeNode> eNodes = new HashMap<String,EdgeNode>();
		Assert.assertTrue(inf.getEdgeNodes().size()==0);
		EdgeNode en = new EdgeNode("test",
				SimulationSetup.defaultEdgeNodeCapabilities);
		inf.setEdgeNodes(eNodes);
		Assert.assertTrue(inf.getEdgeNodes().size()==eNodes.size());
		Assert.assertEquals(eNodes, inf.getEdgeNodes());
	}

	@Test
	public void testSetCloudNodes() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		HashMap<String,CloudDataCenter> cNodes = new HashMap<String,CloudDataCenter>();
		Assert.assertTrue(inf.getCloudNodes().size()==0);
		CloudDataCenter cdc = new CloudDataCenter("test",
				SimulationSetup.defaultCloudCapabilities);
		inf.setCloudNodes(cNodes);
		Assert.assertTrue(inf.getCloudNodes().size()==cNodes.size());
		Assert.assertEquals(cNodes, inf.getCloudNodes());
	}

	@Test
	public void testToString() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		HashMap<String,CloudDataCenter> cNodes = inf.getCloudNodes();
		HashMap<String,EdgeNode> eNodes = inf.getEdgeNodes();
		HashMap<String,MobileDevice> mDevices = inf.getMobileDevices();
		String toString = inf.toString();
		String tmp = "CLOUD NODES:\n"+cNodes+" ;\nEDGE NODES:\n"+eNodes+" ;\nMOBILE DEVICES:\n"+mDevices+".\n";
		Assert.assertEquals(tmp, toString);
	}

	@Test
	public void testAddPrices() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Coordinates coords = new Coordinates(1.0,2.0);
		Assert.assertNotNull(inf.getPriceForLocation(coords,0));
		ArrayList<Double> trace = new ArrayList<Double>();
		trace.add(1.0);
		trace.add(3.0);
		ElectricityPriceTrace t = new ElectricityPriceTrace(trace);
		inf.addPrices(coords, t);
		Assert.assertNotNull(inf.getPriceForLocation(coords, 0));
	}

}
