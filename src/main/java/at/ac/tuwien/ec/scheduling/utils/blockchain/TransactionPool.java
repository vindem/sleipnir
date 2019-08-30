package at.ac.tuwien.ec.scheduling.utils.blockchain;
import java.util.Random;

public class TransactionPool {
	
	private int transactionCount = 0;
	Random rand;
	final int transNumPerRound = 50;
	
	public TransactionPool()
	{
		rand = new Random();
		transactionCount = 0;
	}
	
	public int getTransactionCount()
	{
		return transactionCount;
	}
	
	public synchronized void generateTransactions()
	{
		transactionCount+=rand.nextInt(transNumPerRound);
	}
	
	public synchronized void createBlockOfSize(int s)
	{
		transactionCount -= s;
	}

}
