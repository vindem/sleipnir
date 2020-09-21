package at.ac.tuwien.ec.model;

public enum Timezone {
	INDIANAPOLIS(39.7794476,-86.4129515,-6),
	DETROIT(42.3526257,-83.2392927,-6),
	DUBLIN(53.3239919,-6.5258936,0),
	STGHISLAIN(50.489824,3.7378795,0),
	SINGAPORE(1.3139961,103.7041579,7),
	KOREA(37.5650172,126.8494614,8);
	
	private final double xCoord,yCoord;
	private final int timeOffset;
	
	Timezone(double x,double y, int offset){
		xCoord = x;
		yCoord = y;
		timeOffset = offset;
	}
	
	public double getX()
	{
		return xCoord;
	}
	
	public double getY()
	{
		return yCoord;
	}
	
	public int getTZOffset(){
		return timeOffset;
	}

}
