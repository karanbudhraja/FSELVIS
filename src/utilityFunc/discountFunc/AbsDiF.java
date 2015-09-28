package utilityFunc.discountFunc;

public abstract class AbsDiF {
	
	public double apply(double baseReward) {
		return baseReward * getMultiplier(baseReward);
	}
	
	protected abstract double getMultiplier(double baseReward);
}
