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

public class CompareSellersIndividually {

	private static double	BASE_Q			= 3;
	private static double	EPSILON			= 0.0;
	private static double	LEARNING_RATE	= 1;
	private static double	DISCOUNT_FACTOR	= 1;
	
	private static boolean	IS_VERBOSE		= false;
	private static int		NUM_GAMES		= 2000;
	//optimal: 2->1->3
	private static int	 	NUM_ROUNDS		= 30;
	private static double 	THRESHOLD 		= 50;
	private static int		NUM_FEAT		= 8; 
	private static double	DISCOUNT		= 0.5;
	private static double	START_GUESS		= 75.001;
	private static double	ACCURACY		= 0.95;
	
	private static String IN_PATH = "./input/letter_dataset/";
	private static String	OUT_PATH	= "./learningTest.txt";
	
	private static Writer myWriter;
	private static int counter;

	private static double sigmoid(double x)
	{
	    return 1 / (1 + Math.exp(-x));
	}

	public static void main(String[] args) {
		counter = 0;
		
		System.out.println("Running LearningTest");
		myWriter = new Writer(OUT_PATH);
		myWriter.toBuffer("numRounds, threshold, numFeat, avgUtility, startGuess, accuracy, advUtility, selUtility\r\n");
		myWriter.write();
		
		runExperiment(NUM_GAMES, NUM_ROUNDS, THRESHOLD, NUM_FEAT, DISCOUNT, START_GUESS, ACCURACY);
		
		System.out.println("Begin write");
		myWriter.write();
		System.out.println("Done!");
	}

	public static void runExperiment(int numGames, int numRounds, 
			double threshold, int numFeat, double discount, double startGuess, double accuracy) {
		System.out.println(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				discount + ", " + 
				startGuess + ", " + 
				accuracy);
		System.out.println(++counter);
				
		double advSum = 0;
		double selSum = 0;
		
		//make utility function
		Constant discountFunction = new Constant(discount);
		//Linear discountFunction = new Linear(-0.05, 1);
		
		NDimen utilityFunction = new NDimen(discountFunction, IN_PATH, 1);
		
		/* set up sellers */
		int numBuyers = 1;
		//each seller maintains individual binary search instances for individual sellers
		//a seller is then represented as an array of binary search objects
		ArrayList<ArrayList<AbsAdv>> adversaryList = new ArrayList<ArrayList<AbsAdv>>();
		
		ArrayList<AbsAdv> adversary1 = new ArrayList<AbsAdv>();
		for(int id=0; id<numBuyers; id++){
			AbsAdv adversaryEntity = new LearningWithBinarySearch(startGuess, accuracy, utilityFunction, 
					BASE_Q, EPSILON, LEARNING_RATE, DISCOUNT_FACTOR);			
			adversaryEntity.setVerbose(IS_VERBOSE);
			adversary1.add(adversaryEntity);
		}
		adversaryList.add(adversary1);

		ArrayList<AbsAdv> adversary2 = new ArrayList<AbsAdv>();
		for(int id=0; id<numBuyers; id++){
			//AbsAdv adversaryEntity = new LearningWithBinarySearch(startGuess, accuracy, utilityFunction, 
			//		BASE_Q, EPSILON, LEARNING_RATE, DISCOUNT_FACTOR);			
			AbsAdv adversaryEntity = new BinarySearch(startGuess, accuracy, utilityFunction);
			adversaryEntity.setVerbose(IS_VERBOSE);
			adversary2.add(adversaryEntity);
		}
		adversaryList.add(adversary2);
		
		for(int j = 0; j < numGames; j++) {

			/* set up buyers */
			//make selector
			//we now maintain a list of buyers, which do not model the sellers
			//if we wish to model the sellers, then one buyer can be represented as multiple buyers
			//in a manner similar to seller implementation
			ArrayList<AbsSel> selectorList = new ArrayList<AbsSel>();
			
			for(int id=0; id<numBuyers; id++){
				AbsSel selector = new UtilityThreshold(threshold, utilityFunction);
				selector.setVerbose(IS_VERBOSE);
				selectorList.add(selector);
			}			

			// each adversary is a list of adversaries maintaining buyer information
			// reset adversaries
			for(ArrayList<AbsAdv> adversaryEntities : adversaryList){
				for(AbsAdv adversaryEntity : adversaryEntities){
					if(adversaryEntity.getClass().equals(LearningWithBinarySearch.class)){
						((LearningWithBinarySearch)adversaryEntity).resetForLearning(startGuess);
					}
					else{
						((BinarySearch)adversaryEntity).reset(startGuess, accuracy);
					}
					
					adversaryEntity.setVerbose(IS_VERBOSE);
					if(IS_VERBOSE) System.out.println("Game #" + (j+1));
								
					for(int i = 1; i <= numFeat; i++) {					//TODO: Changed i=0; i< to i=1; i<= ; Should work properly (need to double-check)
						adversaryEntity.addFeature(i);
					}
				}
			}
			
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
						
						if(contributingWitnessCount > 0){
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
							// seller's turn is over if item sold
							break;
						}
					}
				}				
			}

			//output total utility 
			AbsAdv adversary = (adversaryList.get(0)).get(0);
			//System.out.println("\n" + adversary.getUtility() + " " + adversary.evaluateUtility() + " " + adversary.getCost());			
			System.out.println("Adversary: utility=" + (adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost()));
			//System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			//System.out.println("---------------"); 
			//((LearningWithBinarySearch)adversary).dumpPolicy();

			advSum = adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost(); 
			//selSum = selector.getUtility() + selector.evaluateUtility() - selector.getCost();
		
		myWriter.toBuffer(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				discount + ", " + 
				startGuess + ", " + 
				accuracy + ", " + 
				(advSum) + ", " + 
				(selSum) + "\r\n");
		}
	}
}
