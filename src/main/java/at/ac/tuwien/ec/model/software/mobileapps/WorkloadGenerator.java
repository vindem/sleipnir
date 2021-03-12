package at.ac.tuwien.ec.model.software.mobileapps;

import org.apache.commons.lang.math.RandomUtils;
import at.ac.tuwien.ec.model.software.MobileWorkload;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class WorkloadGenerator {

	private double[] appDistribution =
	{
		OffloadingSetup.antivirusDistr, OffloadingSetup.chessDistr, OffloadingSetup.facebookDistr, OffloadingSetup.facerecDistr,
		OffloadingSetup.navigatorDistr
	};
	
	public MobileWorkload setupWorkload(int appExecutions, String mobileId){
		MobileWorkload mwl = new MobileWorkload();
		mwl.setUserId(mobileId);
		mwl.setWorkloadId(0);
		String sApp;
			
		for(int i = 0; i < appExecutions; i++)
		{
			sApp = randomApplicationSelection(appDistribution);
			switch(sApp){
			case "NAVI":
				mwl.joinSequentially(new NavigatorApp(i,mobileId));
				break;
			case "CHESS":
				mwl.joinSequentially(new ChessApp(i,mobileId));
				break;
			case "ANTIVIRUS":
				mwl.joinSequentially(new AntivirusApp(i,mobileId));
				break;
			case "FACEREC":
				mwl.joinSequentially(new FacerecognizerApp(i,mobileId));
				break;
			case "FACEBOOK":
				mwl.joinSequentially(new FacebookApp(i,mobileId));
				break;
			}
			
		}
		return mwl;
    }
	
	private String randomApplicationSelection(double[] appDistribution) {
		String[] apps = {"ANTIVIRUS", "CHESS", "FACEBOOK", "FACEREC", "NAVI"};
		double c = 0.0;
		double r = Math.random();
		for(int i = 0; i < 5; i++)
		{
			c = c + appDistribution[i];
			if(r <= c)
				return apps[i];
		}
		return null;
	}

	private static String drawApp() {
		double appF = RandomUtils.nextDouble();
		if(appF >= 0 && appF <= 0.45)
			return "FACEBOOK";
		if(appF > 0.45 && appF <= 0.75)
			return "NAVI";
		if(appF > 0.75 && appF <= 0.85)
			return "FACEREC";
		if(appF > 0.85 && appF <= 0.95)
			return "CHESS";
		if(appF > 0.95 && appF <= 1.0)
			return "ANTIVIRUS";
		return null;
	}
}
