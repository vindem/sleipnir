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
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop.CPOPBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop.CPOPRuntime;
import java.util.ArrayList;
import scala.Tuple2;

public class ThesisMain {
  public static void main(String[] args) {
    System.out.println("Testing started");
    double avgRunTime = 0;
    double avgBatteryConsumption = 0;
    double avgExecutionTime = 0;
    double rounds = 100;

    int run = 1;

    for (int i = 1; i <= rounds; i++) {
      ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples =
          generateSamples(1);

      for (Tuple2<MobileApplication, MobileCloudInfrastructure> sample : inputSamples) {
        ArrayList<OffloadScheduling> offloads = null;
        if (run == 0) {
          HEFTResearch alg = new HEFTResearch(sample);
          offloads = alg.findScheduling();
        } else if (run == 1) {
          CPOPRuntime alg = new CPOPRuntime(sample);
          offloads = alg.findScheduling();
        } else if (run == 3) {
          CPOPBattery alg = new CPOPBattery(sample);
          offloads = alg.findScheduling();
        } else if (run == 2) {
          HEFTBattery alg2 = new HEFTBattery(sample);
          offloads = alg2.findScheduling();
        }

        if (offloads != null)
          for (OffloadScheduling os : offloads) {
            System.out.println(
                "[i] = "
                    + i
                    + " | "
                    + os.getRunTime()
                    + ", "
                    + os.getBatteryLifetime()
                    + " [ "
                    + os.getExecutionTime()
                    + "]");
            avgRunTime += os.getRunTime();
            avgBatteryConsumption += os.getBatteryLifetime();
            avgExecutionTime += os.getExecutionTime();
          }
      }
    }

    System.out.println(
        "Result: "
            + avgRunTime / rounds
            + ", "
            + avgBatteryConsumption / rounds
            + " ["
            + avgExecutionTime / rounds
            + "]");
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
