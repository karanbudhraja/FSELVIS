package utilityFunc.discountFunc;

public class Linear extends AbsDiF {
	protected double mSlope;
	protected double mIntersept;

	public Linear (double newSlope, double newIntersept) {
		mSlope = newSlope;
		mIntersept = newIntersept;
	}
	
	protected double getMultiplier(double baseReward) {
		double toReturn = mSlope*baseReward + mIntersept;
		if(toReturn > 1) {
			return 1;
		}
		else if(toReturn < 0) {
			return 0;
		}
		else {
			return toReturn;
		}
	}
}
