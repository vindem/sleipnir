package at.ac.tuwien.ec.scheduling.algorithms.heftbased.utils;

import java.util.Comparator;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class NodeRankComparator implements Comparator<MobileSoftwareComponent> {

	@Override
	public int compare(MobileSoftwareComponent o1, MobileSoftwareComponent o2) {
		return Double.compare(o2.getRank(),o1.getRank());
	}

}
