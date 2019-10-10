package at.ac.tuwien.ec.scheduling.utils.blockchain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.Hardware;

public class TransactionPool implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5066932792324980298L;
	private int transactionCount = 0;
	Random rand;
	final int transNumPerRound = 50;
	private ArrayList<Transaction> transactionPool;
	
	public TransactionPool()
	{
		rand = new Random();
		transactionCount = 0;
		transactionPool = new ArrayList<Transaction>();
		generateTransactions();
	}
	
	public int getTransactionCount()
	{
		return transactionCount;
	}
	
	public synchronized void generateTransactions()
	{
		int newT = 10;
		for(int i = transactionCount; i < transactionCount + newT; i++)
			transactionPool.add(new Transaction("trans_"+i,
					new Hardware(2, 0, 0),
					1.0,
					"all",
					1e6,
					1e6));
		transactionCount += newT;
		
			
	}
	
	public synchronized void createBlockOfSize(int s)
	{
		transactionCount -= s;
		for(int i = 0; i < s; i++)
			transactionPool.remove(i);
	}

	public ArrayList<Transaction> getTransactions() {
		// TODO Auto-generated method stub
		return transactionPool;
	}
	
	public TransactionPool clone()
	{
		TransactionPool cloned = new TransactionPool();
		cloned.transactionPool = (ArrayList<Transaction>) transactionPool.clone();
		cloned.transactionCount = transactionCount;
		return cloned;
		
	}

}
