package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import java.io.Serializable;

public abstract class ComputationalNode extends NetworkedNode implements Serializable {

  /** */
  private static final long serialVersionUID = -3728294299293549641L;

  private class DefaultPriceModel implements PricingModel, Serializable {
    public double computeCost(
        SoftwareComponent sc,
        ComputationalNode cn0,
        ComputationalNode cn,
        MobileCloudInfrastructure i) {
      return 0.0;
    }

    @Override
    public double computeCost(
        SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i) {
      // TODO Auto-generated method stub
      return 0;
    }
  }

  protected CPUEnergyModel cpuEnergyModel;
  protected PricingModel priceModel;
  protected double bandwidth, latency;
  protected SoftwareComponent deployedComponent = null;

  public ComputationalNode(String id, HardwareCapabilities capabilities) {
    super(id, capabilities);
    setPricingModel(new DefaultPriceModel());
  }

  private void setPricingModel(DefaultPriceModel pricingModel) {
    this.priceModel = pricingModel;
  }

  public double getMipsPerCore() {
    return this.capabilities.getMipsPerCore();
  }

  public CPUEnergyModel getCPUEnergyModel() {
    return cpuEnergyModel;
  }

  public void setCPUEnergyModel(CPUEnergyModel cpuEnergyModel) {
    this.cpuEnergyModel = cpuEnergyModel;
  }

  public double computeCost(SoftwareComponent sc, MobileCloudInfrastructure i) {
    return priceModel.computeCost(sc, this, i);
  }

  public double computeCost(
      SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i) {
    return priceModel.computeCost(sc, src, i);
  }

  public boolean deploy(SoftwareComponent sc) {
    deployedComponent = sc;
    return capabilities.deploy(sc);
  }

  public void undeploy(SoftwareComponent sc) {
    deployedComponent = null;
    capabilities.undeploy(sc);
  }

  public abstract void sampleNode();

  public double getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(double bandwidth) {
    this.bandwidth = bandwidth;
  }

  public double getLatency() {
    return latency;
  }

  public void setLatency(double latency) {
    this.latency = latency;
  }

  public void undeploy(ContainerInstance vmInstance) {
    capabilities.undeploy(vmInstance);
  }

  public void deployVM(ContainerInstance vm) {
    capabilities.deploy(vm);
  }

  public SoftwareComponent getDeployedComponent() {
    return deployedComponent;
  }

  public void setDeployedComponent(SoftwareComponent deployedComponent) {
    this.deployedComponent = deployedComponent;
  }
}
