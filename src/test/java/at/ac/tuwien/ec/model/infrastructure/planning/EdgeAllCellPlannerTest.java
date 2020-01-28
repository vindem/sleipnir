package at.ac.tuwien.ec.model.infrastructure.planning;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

class EdgeAllCellPlannerTest {

	@Test
	void testSetupEdgeNodesAllFine() {
		MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
		Assert.assertNotNull(inf);
		EdgeAllCellPlanner.setupEdgeNodes(inf);
		Assert.assertFalse(inf.getEdgeNodes().isEmpty());
		Assert.assertTrue(inf.getEdgeNodes().size() == SimulationSetup.MAP_M * SimulationSetup.MAP_N);
	}

}
