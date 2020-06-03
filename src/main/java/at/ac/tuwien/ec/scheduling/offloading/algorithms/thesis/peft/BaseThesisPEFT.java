package at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.peft;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.thesis.ThesisOffloadScheduler;
import scala.Tuple2;

public abstract class BaseThesisPEFT extends ThesisOffloadScheduler {

  private static final long serialVersionUID = 1153273992020302324L;

  double[][] OCT;

  public BaseThesisPEFT(MobileApplication A, MobileCloudInfrastructure I) {
    super();
    setMobileApplication(A);
    setInfrastructure(I);
  }

  public BaseThesisPEFT(Tuple2<MobileApplication, MobileCloudInfrastructure> t) {
    this(t._1(), t._2());
  }

  @Override
  protected void prepareAlgorithm() {
    OCT = new double[currentApp.getTasks().size()][currentInfrastructure.getAllNodes().size()];
    fillOCT(currentApp, currentInfrastructure);
    setRank(currentApp, currentInfrastructure);
  }

  private void setRank(MobileApplication A, MobileCloudInfrastructure I) {
    double sum = 0.0;
    for (MobileSoftwareComponent msc : A.getTasks()) {
      for (ComputationalNode node : I.getAllNodes())
        sum += OCT[A.getTaskIndex(msc)][I.getAllNodes().indexOf(node)];
      msc.setRank(sum / (I.getAllNodes().size() + 1));
    }
  }

  private void fillOCT(MobileApplication A, MobileCloudInfrastructure I) {
    for (int j = 0; j < I.getAllNodes().size(); j++) OCT[A.getTasks().size() - 1][j] = 0.0;

    for (int i = A.getTasks().size() - 2; i >= 0; i--)
      for (int j = 0; j < I.getAllNodes().size(); j++) computeOCTRank(i, j);
  }

  private void computeOCTRank(int i, int j) {
    MobileSoftwareComponent ti = this.currentApp.getTaskByIndex(i);

    double maxOCTperTJ = 0.0;

    // finding average communication time
    double avgComm = 0.0;
    for (ComponentLink neigh : currentApp.getOutgoingEdgesFrom(ti)) {
      for (ComputationalNode cn0 : currentInfrastructure.getMobileDevices().values()) {
        for (ComputationalNode cn1 : currentInfrastructure.getEdgeNodes().values()) {
          avgComm += currentInfrastructure.getTransmissionTime(neigh.getTarget(), cn0, cn1);
        }

        for (ComputationalNode cn1 : currentInfrastructure.getCloudNodes().values()) {
          avgComm += currentInfrastructure.getTransmissionTime(neigh.getTarget(), cn0, cn1);
        }
      }

      avgComm =
          avgComm
              / (currentInfrastructure.getCloudNodes().size()
                  + currentInfrastructure.getEdgeNodes().size()
                  + 1);
    }

    for (MobileSoftwareComponent tj : currentApp.getNeighbors(ti)) {
      double minOCTperPW = Double.MAX_VALUE;

      for (int k = 0; k < currentInfrastructure.getAllNodes().size(); k++) {
        double tmp =
            OCT[currentApp.getTaskIndex(tj)][k]
                + tj.getLocalRuntimeOnNode(
                    currentInfrastructure.getAllNodes().get(k), currentInfrastructure)
                + avgComm;
        if (tmp < minOCTperPW) minOCTperPW = tmp;
      }
      if (minOCTperPW > maxOCTperTJ) maxOCTperTJ = minOCTperPW;
    }

    OCT[i][j] = maxOCTperTJ;
  }
}
