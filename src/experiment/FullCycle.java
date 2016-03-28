package experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

public class FullCycle {
	private static boolean	IS_VERBOSE		= false;
	private static int		NUM_GAMES		= 100;
	
	private static double 	NUM_ROUNDS_L	= 10;
	private static double 	NUM_ROUNDS_H	= 50;
	
	private static double 	THRESHOLD_L 	= 0.5;
	private static double 	THRESHOLD_H 	= 2.0;
	
	private static double	NUM_FEAT_L		= 8; 					//should be 10
	private static double	NUM_FEAT_H		= 17; 			
	
	private static double	AVG_UTILITY_L	= 0.5;
	private static double	AVG_UTILITY_H	= 2.0;
	
	private static double	START_GUESS_L	= 2.001;
	private static double	START_GUESS_H	= 4.001;
	
	private static double	ACCURACY_L		= 0.5;
	private static double	ACCURACY_H		= 0.99;
	
	private static int 		INCREMENTS		= 5;
	
	private static String IN_PATH = "/users/denizen/Desktop/AfsResults/input/letter";
	private static String	OUT_PATH	= "/users/denizen/Desktop/AfsResults/fullCycle.txt";
	
	private static Writer myWriter;
	private static int counter;
	
	private static double sigmoid(double x)
	{
	    return 1 / (1 + Math.exp(-x));
	}

	public static void main(String[] args) {
		counter = 0;
		
		System.out.println("Running FullCycle");
		myWriter = new Writer(OUT_PATH);
		myWriter.toBuffer("numRounds, threshold, numFeat, avgUtility, startGuess, accuracy, advUtility, selUtility\r\n");
		myWriter.write();
		
		double d = 0; //commented out avg_utility change since it is not being modified
		
		for(int a = 0; a < INCREMENTS; a++)
			for(int b = 0; b < INCREMENTS; b++)
				for(int c = 0; c < INCREMENTS; c++)
					//for(int d = 0; d < INCREMENTS; d++)
						for(int e = 0; e < INCREMENTS; e++)
							for(int f = 0; f < INCREMENTS; f++)
								runExperiment(NUM_GAMES, 
										(int)(	a*(NUM_ROUNDS_H		-NUM_ROUNDS_L)	/INCREMENTS 	+ NUM_ROUNDS_L),
												b*(THRESHOLD_H			-THRESHOLD_L) 		/INCREMENTS	+ THRESHOLD_L, 
										(int)(	c*(NUM_FEAT_H			-NUM_FEAT_L) 			/INCREMENTS	+ NUM_FEAT_L),
												d*(AVG_UTILITY_H		-AVG_UTILITY_L) 		/INCREMENTS	+ AVG_UTILITY_L,
												e*(START_GUESS_H		-START_GUESS_L) 	/INCREMENTS	+ START_GUESS_L,
												f*(ACCURACY_H				-ACCURACY_L) 			/INCREMENTS	+ ACCURACY_L
										);
		
		
		System.out.println("Begin write");
		myWriter.write();
		System.out.println("Done!");
	}
	
	public static void runExperiment(int numGames, int numRounds, 
			double threshold, int numFeat, double avgUtility, double startGuess, double accuracy) {
		/*System.out.println(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				avgUtility + ", " + 
				startGuess + ", " + 
				accuracy);*/
		System.out.println(++counter);
				
		double advSum = 0;
		double selSum = 0;
		for(int j = 0; j < numGames; j++) {
			//make utility function
			//Constant discountFunction = new Constant(0.5);
			Linear discountFunction = new Linear(-0.05, 1);
			Naive1D utilityFunction = new Naive1D(discountFunction, false, IN_PATH);
			for(int i = 0; i < numFeat; i++) {
				utilityFunction.addFeature(i, Math.random()*2.0*avgUtility);
			}

			/* set up sellers */
			//each seller maintains individual binary search instances for individual sellers
			//a seller is then represented as an array of binary search objects
			ArrayList<ArrayList<AbsAdv>> adversaryList = new ArrayList<ArrayList<AbsAdv>>();
			
			ArrayList<AbsAdv> adversary1 = new ArrayList<AbsAdv>();
			for(int id=0; id<2; id++){
				AbsAdv adversaryEntity = new BinarySearch(startGuess, accuracy, utilityFunction, false, 10);
				adversaryEntity.setVerbose(IS_VERBOSE);
				for(int i = 0; i < numFeat; i++) {
					adversaryEntity.addFeature(i);
				}
				adversary1.add(adversaryEntity);
			}
			adversaryList.add(adversary1);
			
			ArrayList<AbsAdv> adversary2 = new ArrayList<AbsAdv>();
			for(int id=0; id<2; id++){
				AbsAdv adversaryEntity = new BinarySearch(startGuess, accuracy, utilityFunction, false, 10);
				adversaryEntity.setVerbose(IS_VERBOSE);
				for(int i = 0; i < numFeat; i++) {
					adversaryEntity.addFeature(i);
				}
				adversary2.add(adversaryEntity);
			}
			adversaryList.add(adversary2);

			/* set up buyers */
			//make selector
			//we now maintain a list of buyers, which do not model the sellers
			//if we wish to model the sellers, then one buyer can be represented as multiple buyers
			//in a manner similar to seller implementation
			ArrayList<AbsSel> selectorList = new ArrayList<AbsSel>();
			
			AbsSel selector1 = new UtilityThreshold(threshold, utilityFunction);
			selector1.setVerbose(IS_VERBOSE);
			selectorList.add(selector1);
			
			AbsSel selector2 = new UtilityThreshold(threshold, utilityFunction);
			selector2.setVerbose(IS_VERBOSE);
			selectorList.add(selector2);
			
/********** main loop ********************************************************************************************************/
			
			/* set up game */
			//run random number of rounds
			for(int i = 0; i < numRounds; i++) {
				
				// each adversary is a list of adversaries maintaining buyer information
				for(ArrayList<AbsAdv> adversary : adversaryList){					
					
					/* information gathering */
					//gather information about sellers from other adversaries
					int k_s = 5;
					double alpha_s = 0.1;
					double witnessScoreThreshold = 0.5;
					
					//select order of requesting adversaries randomly
					ArrayList<Integer> adversaryIndices = new ArrayList<Integer>();
					for(int k = 0; k < adversaryList.size(); k++){
						if(k != adversaryList.indexOf(adversary)){
							adversaryIndices.add(k);
						}
					}
					Collections.shuffle(adversaryIndices);

					//buyers that this adversary knows about are those for which interaction > 0
					// we may later use these number for more complicated methods
					Set<Integer> knownSet = new HashSet<Integer>();
					for(int index = 0; index < adversary.size(); index++){
						if(adversary.get(index).getInteractionCount() > 0){
							knownSet.add(index);
						}
					}
					
					//iterate through candidate witness adversaries
					Set<Integer> witnesses = new HashSet<Integer>();
					for(int adversaryIndex : adversaryIndices){
						Set<Integer> witnessKnownSet = new HashSet<Integer>();
						for(int index = 0; index < adversaryList.get(adversaryIndex).size(); index++){
							if(adversaryList.get(adversaryIndex).get(index).getInteractionCount() > 0){
								witnessKnownSet.add(index);
							}
						}
						
						//compute score for this witness
						witnessKnownSet.removeAll(knownSet);
						Set<Integer> unknownSet = witnessKnownSet;
						double witnessScore;
						
						if(knownSet.size() == 0){
							witnessScore = Double.POSITIVE_INFINITY;
						}
						else{
							witnessScore = sigmoid(unknownSet.size()/knownSet.size());
						}
						
						//participate if score above threshold
						if(witnessScore > witnessScoreThreshold){
							witnesses.add(adversaryIndex);
						}
						
						//we are done if we reach the limit on number of witnesses
						if(witnesses.size() >= k_s){
							break;
						}
					}
			
					//now use witness information to condition adversary model of selectors
					for(int k = 0; k < selectorList.size(); k++){
						double lowerPredictionEstimate = 0;
						double upperPredictionEstimate = 0;
						double accuracyEstimate = 0;
						int contributingWitnessCount = 0;
						
						for(Integer witness : witnesses){
							//only contributes about those buyers that the witness has interacted with
							if(adversaryList.get(witness).get(k).getInteractionCount() > 0){
								contributingWitnessCount++;
								List<Double> modelEstimate = adversaryList.get(witness).get(k).getModelEstimate();

								//taking average for now. can be made more complicated
								lowerPredictionEstimate += modelEstimate.get(0);
								upperPredictionEstimate += modelEstimate.get(1);
								accuracyEstimate += modelEstimate.get(2);
							}
						}
						
						//compute average estimate
						lowerPredictionEstimate /= contributingWitnessCount;
						upperPredictionEstimate /= contributingWitnessCount;
						accuracyEstimate /= contributingWitnessCount;
						
						//account for adversary's own model
						lowerPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(0) + alpha_s*lowerPredictionEstimate;
						upperPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(1) + alpha_s*upperPredictionEstimate;
						accuracyEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(2) + alpha_s*accuracyEstimate;

						//use these estimates to generate cost
						//these changes are currently permanent to the adversary
						//they may later be made temporary per round
						adversary.get(k).setModelEstimate(Arrays.asList(lowerPredictionEstimate, upperPredictionEstimate, accuracyEstimate));
					}
					/* end of information gathering */
					
					//select selectors randomly
					ArrayList<Integer> selectorIndices = new ArrayList<Integer>();
					for(int k = 0; k < selectorList.size(); k++){
						selectorIndices.add(k);
					}
					Collections.shuffle(selectorIndices);
					
					// iterate through selectors
					for(int selectorIndex : selectorIndices){
					
						// try to sell to that buyer
						boolean accepted = adversary.get(selectorIndex).giveOffer(selectorList.get(selectorIndex));
						
						if(accepted == true){
							//remove the feature across all other objects corresponding to adversary
							for(int index = 0; index < adversary.size(); index++){
								if(index != selectorIndex){
									adversary.get(index).processAccept(0, adversary.get(selectorIndex).getMostRecentOffer());
								}
							}
							
							// seller's turn is over if item sold
							break;
						}
					}
				}				
			}

/*****************************************************************************************************************************/

			//output total utility 
	/*		System.out.println("Adversary: utility=" + (adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost()));
			System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			System.out.println("---------------"); 
	*/		
			//using default values for now
			advSum += adversary1.get(0).getUtility(); 
			selSum += selector1.getUtility() - selector1.getCost();
		}
		myWriter.toBuffer(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				avgUtility + ", " + 
				startGuess + ", " + 
				accuracy + ", " + 
				(advSum/(double)numGames) + ", " + 
				(selSum/(double)numGames) + "\r\n");
	}

}
