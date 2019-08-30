package at.ac.tuwien.ec.model.infrastructure;

import at.ac.tuwien.ec.scheduling.utils.blockchain.TransactionPool;

public class MobileBlockchainInfrastructure extends MobileCloudInfrastructure {
	
	private TransactionPool transPool;
	
	public MobileBlockchainInfrastructure()
	{
		super();
		transPool = new TransactionPool();
	}

	public TransactionPool getTransPool() {
		return transPool;
	}

	public void setTransPool(TransactionPool transPool) {
		this.transPool = transPool;
	}

}
