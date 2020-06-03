package at.ac.tuwien.ec.sleipnir.thesis;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultCloudPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.DefaultNetworkPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.edge.EdgeAllCellPlanner;
import at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.DefaultMobileDevicePlanner;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.model.software.mobileapps.WorkloadGenerator;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop.CPOPBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.cpop.CPOPRuntime;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.heft.ThesisHEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.heft.ThesisHEFTRuntime;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla.KDLABattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.kdla.KDLARuntime;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb.MMOLBBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.mmolb.MMOLBRuntime;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.peft.ThesisPEFTBattery;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.peft.ThesisPEFTRuntime;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import java.util.ArrayList;
import scala.Tuple2;

public class ThesisMain {
  public static void main(String[] args) {
    System.out.println("Testing started");
    double avgRunTime = 0;
    double avgBatteryConsumption = 0;
    double avgExecutionTime = 0;
    double rounds = 20;

    int run = 2;

    for (int i = 1; i <= rounds; i++) {
      ArrayList<Tuple2<MobileApplication, MobileCloudInfrastructure>> inputSamples =
          generateSamples(1);

      for (Tuple2<MobileApplication, MobileCloudInfrastructure> sample : inputSamples) {
        ArrayList<OffloadScheduling> offloads = null;
        if (run == -1) {
          offloads = new HEFTResearch(sample).findScheduling();

        } else {
          ThesisOffloadScheduler scheduler = null;

          switch (run) {
            case 0:
              scheduler = new ThesisHEFTRuntime(sample);
              break;
            case 1:
              scheduler = new ThesisHEFTBattery(sample);
              break;
            case 2:
              scheduler = new CPOPRuntime(sample);
              break;
            case 3:
              scheduler = new CPOPBattery(sample);
              break;
            case 4:
              scheduler = new KDLARuntime(sample);
              break;
            case 5:
              scheduler = new KDLABattery(sample);
              break;
            case 6:
              scheduler = new ThesisPEFTRuntime(sample);
              break;
            case 7:
              scheduler = new ThesisPEFTBattery(sample);
              break;
            case 8:
              scheduler = new MMOLBRuntime(sample);
              break;
            case 9:
              scheduler = new MMOLBBattery(sample);
              break;
          }
          offloads = (ArrayList<OffloadScheduling>) scheduler.findScheduling();
        }

        if (offloads != null) {
          for (OffloadScheduling os : offloads) {
            os.forEach(
                (key, value) -> {
                  System.out.println(key.getId() + "->" + value.getId());
                });

            System.out.println(
                "[i] = "
                    + i
                    + " | "
                    + os.getRunTime()
                    + ", "
                    + os.getBatteryLifetime()
                    + " [ "
                    + os.getExecutionTime() / Math.pow(10, 9)
                    + "]");
            avgRunTime += os.getRunTime();
            avgBatteryConsumption += os.getBatteryLifetime();
            avgExecutionTime += os.getExecutionTime();
          }
        }
      }
    }

    double avg_seconds = (avgExecutionTime / rounds) / Math.pow(10, 9);
    double sum_seconds = (avgExecutionTime) / Math.pow(10, 9);

    System.out.println();
    System.out.println("================= FINISHED ================= ");
    System.out.println(
        "Result: "
            + avgRunTime / rounds
            + ", "
            + avgBatteryConsumption / rounds
            + " ["
            + avg_seconds
            + ", "
            + sum_seconds
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
