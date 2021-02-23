package at.ac.tuwien.ec.model.software.mobileapps;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class ChessApp extends MobileApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8691346644863245202L;

	public ChessApp() {
		super();
	}
	
	public ChessApp(int wId)
	{
		super(wId);
	}
	
	public ChessApp(int wId,String uid)
	{
		super(wId,uid);
	}
	
	@Override
	public void sampleTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sampleLinks() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupTasks() {
		for(int i = 0; i < SimulationSetup.chessMovesNum; i++)
		{
			addComponent("CHESS_UI_"+i+"_"+getWorkloadId()+","+getUserId()
					,new Hardware(1, 1, 1)
					,getUserId()
					//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/4.0) + 4.0)
					,4.0e3
					,5e+3
					,5e+3
					,false
					);
			addComponent("UPDATE_CHESS_"+i+"_"+getWorkloadId()+","+getUserId()
					,new Hardware(1, 1, 1)
					,getUserId()
					//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0) + 2.0)
					,2.0e3
					,5e3
					,5e3
					);
			addComponent("COMPUTE_MOVE_"+i+"_"+getWorkloadId()+","+getUserId()
					,new Hardware(1,2,1)
					,getUserId()
					//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/SimulationSetup.chess_mi) + SimulationSetup.chess_mi)
					,OffloadingSetup.chessMI					
					,5e+3
					,5e+3
					);
			addComponent("CHESS_OUTPUT_"+i+"_"+getWorkloadId()+","+getUserId()
					,new Hardware(1, 1, 1)
					,this.getUserId()
					//,Math.ceil(ExponentialDistributionGenerator.getNext(1.0/2.0)+1.0)
					,2.0e3
					,5e3
					,5e3
					,false
					);
		}
	}

	@Override
	public void setupLinks() {
		for(int i = 0; i < SimulationSetup.chessMovesNum - 1; i++)
		{
			addLink("CHESS_UI_"+i+"_"+getWorkloadId()+","+getUserId()
					, "UPDATE_CHESS_"+i+"_"+getWorkloadId()+","+getUserId(),
					sampleLatency(),
					0.1);
			addLink("UPDATE_CHESS_"+i+"_"+getWorkloadId()+","+getUserId(),
					"COMPUTE_MOVE_"+i+"_"+getWorkloadId()+","+getUserId(),
					sampleLatency(),
					0.1);
			addLink("COMPUTE_MOVE_"+i+"_"+getWorkloadId()+","+getUserId()
					,"CHESS_OUTPUT_"+i+"_"+getWorkloadId()+","+getUserId(),
					sampleLatency(),
					0.1);
			addLink("CHESS_OUTPUT_"+i+"_"+getWorkloadId()+","+getUserId(),
					"CHESS_UI_"+(i+1)+"_"+getWorkloadId()+","+getUserId(),
					sampleLatency(),
					0.1);
		}
		addLink("CHESS_UI_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId()
				, "UPDATE_CHESS_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("UPDATE_CHESS_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId()
				, "COMPUTE_MOVE_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
		addLink("COMPUTE_MOVE_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId(),
				"CHESS_OUTPUT_"+(OffloadingSetup.chessMovesNum-1)+"_"+getWorkloadId()+","+getUserId(),
				sampleLatency(),
				0.1);
	}

}
