package agent.adversary;

import java.util.ArrayList;

import agent.AbsAgent;
import agent.selector.*;
import utilityFunc.AbsUtF;

public abstract class AbsAdv extends AbsAgent{
	protected ArrayList<Integer> mSellHistory;
	
	public AbsAdv(AbsUtF newFunction) {
		super(newFunction);
		mSellHistory = new ArrayList<Integer>();
	}
	
	public abstract void giveOffer(AbsSel target);

	public void processAccept(double cost, int index) {
		if(mVerbose) System.out.println("Cost was: " + cost);
		mUtility += cost;
		mFeatures.remove((Integer)index);
		mSellHistory.add((Integer)index);
	}
}
