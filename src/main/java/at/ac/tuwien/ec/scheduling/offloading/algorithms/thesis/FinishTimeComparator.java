package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import java.util.Comparator;

public class FinishTimeComparator implements Comparator<MobileSoftwareComponent> {

  @Override
  public int compare(MobileSoftwareComponent o1, MobileSoftwareComponent o2) {
    return Double.compare(o1.getFinishTime(),o2.getFinishTime());
  }

}
