package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

import java.lang.Math;

public class CompareSellersIndividually {

	private static boolean	IS_VERBOSE		= false;
	private static boolean	IS_OVERWRITE	= true;
	//experiment settings
	private static int		NUM_RUNS				= 1000; 
	private static int		NUM_BUYERS				= 1;
	private static int		NUM_LEARN_SELLERS 		= 0; //LearningWithBinarySearch
	private static int		NUM_BASIC_SELLERS 		= 2; //BinarySearch (NOT learning)
	private static boolean	IS_ONE_SALE_PER_ROUND 	= true;
	private static boolean	IS_NAIVE_UTIL_FUNC		= true;
	private static boolean	IS_MULTIPLES			= false;
	//witness settings
	private static boolean	IS_INFORMATION_SHARED 	= false;
	private static int 		K_S 					= 5;
	private static double 	ALPHA_S 				= 1.0;
	private static double 	WITNESS_SCORE_THRESHOLD = 0.0;
	//q-learning parameters
	private static double	BASE_Q			= 30;
	private static double	EPSILON			= 0.0;
	private static double	LEARNING_RATE	= 0.1;
	private static double	DISCOUNT_FACTOR	= 0.99;
	//game settings
	private static int		NUM_GAMES		= 3000;
	private static int	 	NUM_ROUNDS		= 40;
	private static double 	THRESHOLD 		= 50;
	private static int		NUM_FEAT		= 8; 
	private static double	DISCOUNT		= 0.5;
	private static double	START_GUESS		= 85.001;
	private static double	ACCURACY		= 0.95;
	//io paths
	private static String	IN_PATH = "./input/letter_dataset/letter";
	private static String	OUT_PATH	= "./outFolder/learningTest";
	//indices to different parts of the data in file
	private static int		F_NUM_ROUNDS	= 0;
	private static int		F_THRESHOLD		= 1;
	private static int		F_NUM_FEAT		= 2;
	private static int		F_DISCOUNT		= 3;
	private static int		F_START_GUESS	= 4;
	private static int		F_ACCURACY		= 5;
	private static int		F_ADV_UTILITY	= 6;
	private static int		F_SEL_UTILITY	= 7;
	
	private static Writer myWriter;
	private static int counter;

	private static double sigmoid(double x)
	{
	    return 1 / (1 + Math.exp(-x));
	}
	//java does not support type conversion so we need this for speed
	private static HashMap<Boolean, Integer> boolToInt = new HashMap<Boolean, Integer>() {{
		put(true, 1);
		put(false, 0);
	}};

	// statistical values
	private static double getMean(ArrayList<Double> data)
    {
        double sum = 0;
        for(int i = 0; i < data.size(); i++){
        	   sum += data.get(i); // this is the calculation for summing up all the values
    	}
        return sum/data.size();
    }

	private static double getVariance(ArrayList<Double> data)
    {
        double mean = getMean(data);
        double temp = 0;
        for(int i = 0; i < data.size(); i++){
            temp += Math.pow(mean-data.get(i), 2);
        }
        return temp/data.size();
    }

    private static double getStdDev(ArrayList<Double> data)
    {
        return Math.sqrt(getVariance(data));
    }

	private static double getError(ArrayList<Double> data)
    {
        double max = Collections.max(data);
        double min = Collections.min(data);
        return (max-min)/2;
    }
    
	public static void main(String[] args) {
		counter = 0;
		
		//run all experiments
		for(int e = 0; e < NUM_RUNS; e++) {
			System.out.println("Running LearningTest #" + (e+1));
			
			//set up file writing
			myWriter = new Writer(OUT_PATH + "_" + (e+1) + ".txt", IS_OVERWRITE);
			
			//run experiment
			runExperiment(NUM_GAMES, NUM_ROUNDS, THRESHOLD, NUM_FEAT, DISCOUNT, START_GUESS, ACCURACY);
			
			//write file
			System.out.println("Begin write");
			myWriter.write();
			System.out.println("Done!");
		}
		
		//set up file writer
		myWriter = new Writer(OUT_PATH + ".txt", IS_OVERWRITE);
		myWriter.toBuffer("numRounds, threshold, numFeat, DISCOUNT, startGuess, accuracy, advUtility, selUtility\r\n");
		myWriter.write();

		//open files to compute average
		BufferedReader[] myBufferReader = new BufferedReader[NUM_RUNS];
		String[] fileBuffer = new String[NUM_RUNS];
		
		try {
				for(int e = 0; e < NUM_RUNS; e++) {
					//open buffer
					myBufferReader[e] = new BufferedReader(new FileReader(OUT_PATH + "_" + (e+1) + ".txt"));
				}

				while (true) {
					//read one line
					double statNumRounds = 0;
					double statThreshold = 0;
					double statNumfeat = 0;
					double statDiscount = 0;
					double statStartGuess = 0;
					double statAccuracy = 0;
					double statAdvUtility = 0;
					double statSelUtility = 0;

					ArrayList<Double> numRoundsList = new ArrayList<Double>();
					ArrayList<Double> thresholdList = new ArrayList<Double>();
					ArrayList<Double> numFeatList = new ArrayList<Double>();
					ArrayList<Double> discountList = new ArrayList<Double>();
					ArrayList<Double> startGuessList = new ArrayList<Double>();
					ArrayList<Double> accuracyList = new ArrayList<Double>();
					ArrayList<Double> advUtilityList = new ArrayList<Double>();
					ArrayList<Double> selUtilityList = new ArrayList<Double>();
					
					for(int e = 0; e < NUM_RUNS; e++) {
						try {								
								//check end of file
								fileBuffer[e] = myBufferReader[e].readLine();
								if(fileBuffer[e] == null){
									//delete all individual files
									for(int f = 0; f < NUM_RUNS; f++) {
										File file = new File(OUT_PATH + "_" + (f+1) + ".txt");
										file.delete();
									}

									//end of averaging
									return;
								}
								
								String[] tokenizedBuffer = fileBuffer[e].split(", ");
								
								numRoundsList.add(Double.parseDouble(tokenizedBuffer[F_NUM_ROUNDS]));
								thresholdList.add(Double.parseDouble(tokenizedBuffer[F_THRESHOLD]));
								numFeatList.add(Double.parseDouble(tokenizedBuffer[F_NUM_FEAT]));
								discountList.add(Double.parseDouble(tokenizedBuffer[F_DISCOUNT]));
								startGuessList.add(Double.parseDouble(tokenizedBuffer[F_START_GUESS]));
								accuracyList.add(Double.parseDouble(tokenizedBuffer[F_ACCURACY]));
								advUtilityList.add(Double.parseDouble(tokenizedBuffer[F_ADV_UTILITY]));
								selUtilityList.add(Double.parseDouble(tokenizedBuffer[F_SEL_UTILITY]));
								
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}	
					
					//compute average
					statNumRounds = getMean(numRoundsList);
					statThreshold = getMean(thresholdList);
					statNumfeat = getMean(numFeatList);
					statDiscount = getMean(discountList);
					statStartGuess = getMean(startGuessList);
					statAccuracy = getMean(accuracyList);
					statAdvUtility = getMean(advUtilityList);
					statSelUtility = getMean(selUtilityList);
					
					myWriter.toBuffer(
							statNumRounds + ", " +
							statThreshold + ", " + 
							statNumfeat + ", " + 
							statDiscount + ", " + 
							statStartGuess + ", " + 
							statAccuracy + ", " + 
							statAdvUtility + ", " + 
							statSelUtility + "\r\n");
					myWriter.write();

					//compute standard deviation
					statNumRounds = getStdDev(numRoundsList);
					statThreshold = getStdDev(thresholdList);
					statNumfeat = getStdDev(numFeatList);
					statDiscount = getStdDev(discountList);
					statStartGuess = getStdDev(startGuessList);
					statAccuracy = getStdDev(accuracyList);
					statAdvUtility = getStdDev(advUtilityList);
					statSelUtility = getStdDev(selUtilityList);
					
					myWriter.toBuffer(
							statNumRounds + ", " +
							statThreshold + ", " + 
							statNumfeat + ", " + 
							statDiscount + ", " + 
							statStartGuess + ", " + 
							statAccuracy + ", " + 
							statAdvUtility + ", " + 
							statSelUtility + "\r\n");
					myWriter.write();
				}				
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
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
		
		AbsUtF utilityFunction;
		if(!IS_NAIVE_UTIL_FUNC) {
			utilityFunction = new NDimen(discountFunction, IS_MULTIPLES, IN_PATH, 1);
		}
		else {
			utilityFunction = new Naive1D(discountFunction, IS_MULTIPLES, IN_PATH);
		}
		
		/* set up sellers */
		//each seller maintains individual binary search instances for individual sellers
		//a seller is then represented as an array of binary search objects
		ArrayList<ArrayList<AbsAdv>> adversaryList = new ArrayList<ArrayList<AbsAdv>>();
		
		for(int k = 0; k < NUM_LEARN_SELLERS; k++) {
			ArrayList<AbsAdv> lwbsAdversary = new ArrayList<AbsAdv>();
			for(int id=0; id<NUM_BUYERS; id++){
				AbsAdv adversaryEntity = new LearningWithBinarySearch(startGuess, accuracy, utilityFunction, 
						BASE_Q, EPSILON, LEARNING_RATE, DISCOUNT_FACTOR, false, 10);			
				adversaryEntity.setVerbose(IS_VERBOSE);
				lwbsAdversary.add(adversaryEntity);
			}
			adversaryList.add(lwbsAdversary);
		}
		
		for(int j = 0; j < numGames; j++) {

			//remove everything from adversaryList if the entry is type BS
			//add a new entry for BS
			//copying to a new object for now
			ArrayList<ArrayList<AbsAdv>> newAdversaryList = new ArrayList<ArrayList<AbsAdv>>();
			for(ArrayList<AbsAdv> adversaryEntities : adversaryList){
				if(adversaryEntities.get(0).getClass().equals(LearningWithBinarySearch.class)){
					newAdversaryList.add(adversaryEntities);
				}
			}
			adversaryList.clear();
			adversaryList = newAdversaryList;

			for(int k = 0; k < NUM_BASIC_SELLERS; k++) {
				ArrayList<AbsAdv> bsAdversary = new ArrayList<AbsAdv>();
				for(int id=0; id<NUM_BUYERS; id++){
					AbsAdv adversaryEntity = new BinarySearch(startGuess, accuracy, utilityFunction, false, 10);
					adversaryEntity.setVerbose(IS_VERBOSE);
					bsAdversary.add(adversaryEntity);
				}
				adversaryList.add(bsAdversary);
			}
				
			/* set up buyers */
			//make selector
			//we now maintain a list of buyers, which do not model the sellers
			//if we wish to model the sellers, then one buyer can be represented as multiple buyers
			//in a manner similar to seller implementation
			ArrayList<AbsSel> selectorList = new ArrayList<AbsSel>();
			
			for(int id=0; id<NUM_BUYERS; id++){
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
					
					adversaryEntity.setVerbose(IS_VERBOSE);
					if(IS_VERBOSE) System.out.println("Game #" + (j+1));
								
					for(int i = 1; i <= numFeat; i++) {
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
					int k_s = K_S;
					double alpha_s = ALPHA_S;
					double witnessScoreThreshold = WITNESS_SCORE_THRESHOLD;
					
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
						double lowerPredictionEstimate = Double.MIN_VALUE;
						double upperPredictionEstimate = Double.MAX_VALUE;
						int contributingWitnessCount = 0;
						
						for(Integer witness : witnesses){
							//only contributes about those buyers that the witness has interacted with
							if(adversaryList.get(witness).get(k).getInteractionCount() > 0){
								contributingWitnessCount++;
								List<Double> modelEstimate = adversaryList.get(witness).get(k).getModelEstimate();

								//not taking average; instead, using witness with most info
								if(modelEstimate.get(0) > lowerPredictionEstimate)
									lowerPredictionEstimate = modelEstimate.get(0);
								if(modelEstimate.get(1) < upperPredictionEstimate)
									upperPredictionEstimate = modelEstimate.get(1);
								//lowerPredictionEstimate += modelEstimate.get(0);
								//upperPredictionEstimate += modelEstimate.get(1);
							}
						}
						
						if(contributingWitnessCount > 0){
							//no need for average since the most extreme value is taken
							//lowerPredictionEstimate /= contributingWitnessCount;
							//upperPredictionEstimate /= contributingWitnessCount;
							
							//condition based on information sharing
							//faster than an if condition
							alpha_s = alpha_s*boolToInt.get(IS_INFORMATION_SHARED);
							
							//account for adversary's own model
							lowerPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(0) + alpha_s*lowerPredictionEstimate;
							upperPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(1) + alpha_s*upperPredictionEstimate;
							
							//use these estimates to generate cost
							//these changes are currently permanent to the adversary
							//they may later be made temporary per round							
							adversary.get(k).setModelEstimate(Arrays.asList(lowerPredictionEstimate, upperPredictionEstimate));
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
						
						if(accepted == true && IS_ONE_SALE_PER_ROUND){
							// seller's turn is over if item sold
							break;
						}
					}
				}				
			}

			//output total utility 
			AbsAdv adversary = (adversaryList.get(0)).get(0);
			//System.out.println("\n" + adversary.getUtility() + " " + adversary.evaluateUtility() + " " + adversary.getCost());			
			System.out.println("Adversary: utility=" + (adversary.getUtility()/* + adversary.evaluateUtility() - adversary.getCost()*/));
			//System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			//System.out.println("---------------"); 
			//((LearningWithBinarySearch)adversary).dumpPolicy();

			advSum = adversary.getUtility(); 
			//selSum = selector.getUtility() - selector.getCost();
		
		myWriter.toBuffer(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				discount + ", " + 
				startGuess + ", " + 
				accuracy + ", " + 
				advSum + ", " + 
				selSum + "\r\n");
		}
	}
}
