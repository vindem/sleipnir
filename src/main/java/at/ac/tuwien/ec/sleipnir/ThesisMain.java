package at.ac.tuwien.ec.sleipnir;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.model.software.mobileapps.WorkloadGenerator;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop.CPOPRuntime;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTResearch;
import java.util.ArrayList;
import scala.Tuple2;

public class ThesisMain {
  public static void main(String[] args) {
    System.out.println("Testing started");
    ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples =
        generateSamples(1);

    inputSamples.forEach(
        sample -> {
          boolean runHeft = true;
          if (runHeft) {

            HEFTResearch alg2 = new HEFTResearch(sample);
            System.out.println("HEFT INIT");

            ArrayList<OffloadScheduling> offloads2 = alg2.findScheduling();
            System.out.println("HEFT finished");

            if (offloads2 != null)
              for (OffloadScheduling os : offloads2) {
                System.out.println(os.getRunTime());
              }
          } else {
            CPOPRuntime alg = new CPOPRuntime(sample);
            System.out.println("CPOP INIT");

            ArrayList<OffloadScheduling> offloads = alg.findScheduling();
            System.out.println("CPOP finished");

            if (offloads != null)
              for (OffloadScheduling os : offloads) {
                System.out.println(os.getRunTime());
              }
          }
        });
  }

  private static ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> generateSamples(
      int iterations) {
    ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> samples =
        new ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>>();
    for (int i = 0; i < iterations; i++) {
      MobileWorkload globalWorkload = new MobileWorkload();
      WorkloadGenerator generator = new WorkloadGenerator();
      for (int j = 0; j < SimulationSetup.mobileNum; j++)
        globalWorkload.joinParallel(
            generator.setupWorkload(SimulationSetup.appNumber, "mobile_" + j));

      MobileCloudInfrastructure inf = new MobileCloudInfrastructure();
      DefaultCloudPlanner.setupCloudNodes(inf, SimulationSetup.cloudNum);
      EdgeAllCellPlanner.setupEdgeNodes(inf);
      DefaultMobileDevicePlanner.setupMobileDevices(inf, SimulationSetup.mobileNum);
      DefaultNetworkPlanner.setupNetworkConnections(inf);
      Tuple2<MobileApplication, MobileCloudInfrastructure> singleSample =
          new Tuple2<MobileApplication, MobileCloudInfrastructure>(globalWorkload, inf);
      samples.add(singleSample);
    }
    return samples;
  }
}
