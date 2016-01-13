package agent.adversary;

import java.util.ArrayList;
import java.util.List;

import agent.AbsAgent;
import agent.selector.*;
import utilityFunc.AbsUtF;

public abstract class AbsAdv extends AbsAgent{
	protected ArrayList<Integer> mSellHistory;
	
	public AbsAdv(AbsUtF newFunction) {
		super(newFunction);
		mSellHistory = new ArrayList<Integer>();
	}
	
	public abstract boolean giveOffer(AbsSel target);

	public void processAccept(double cost, int index) {
		if(mVerbose) System.out.println("Cost was: " + cost);
		mUtility += cost;
		mFeatures.remove((Integer)index);
		mSellHistory.add((Integer)index);
	}
	
	public void processDecline(double cost, int index) {
		//do nothing
	}
	
	// karan adding a method to get last feature offered
	public abstract int getMostRecentOffer();
	public abstract int getInteractionCount();
	public abstract List<Double> getModelEstimate();
	public abstract void setModelEstimate(List<Double> modelEstimate);

}
