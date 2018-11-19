package at.ac.tuwien.ec.scheduling.utils;

import java.util.Comparator;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class RuntimeComparator implements Comparator<MobileSoftwareComponent> {

	@Override
	public int compare(MobileSoftwareComponent o1, MobileSoftwareComponent o2) {
		return Double.compare(o1.getRunTime(),o2.getRunTime());
	}

}
