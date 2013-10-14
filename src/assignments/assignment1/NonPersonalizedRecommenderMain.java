package assignments.assignment1;

import java.io.File;
import java.util.List;

import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.scored.ScoredId;


public class NonPersonalizedRecommenderMain {
	private static String dataFile = "./data/assignment1/";
	private static final int ALGO = 1;
	private static final int N = 5;
	
				
	public static void main(String[] args) {
//		dataFile = dataFile + "recsys-data-ratings-small-proc.csv";
		dataFile = dataFile + "recsys-data-ratings.csv";
		int[] itemsX = new int[] { 11, 121, 8587, 194, 550, 329 };
	
		// Load the data source from file
		CSVDataSourceBuilder csvBuilder = new CSVDataSourceBuilder(new File(dataFile));
		csvBuilder.setCache(true);
		DataSource dataSource = csvBuilder.build();
		System.out.println("Data was loaded from file");
		
		// Create a Data Model
		NonPersonalizedDataModel dataModel = NonPersonalizedDataModel.createDataModel(dataSource);
		System.out.println("Data Model was created successfully");
		
		
		NonPersonalizedScorer scorer = new NonPersonalizedScorer(getAssociativityAlgorithm(), dataModel);
		
		NonPersonalizedTopNRecommender recommender = new NonPersonalizedTopNRecommender(scorer);
		
		for(int itemX : itemsX) {
			List<ScoredId> topN = recommender.globalRecommend(itemX, N, dataModel.getItems());
			printFormatedOutput(itemX, topN);
		}
		
		System.out.println("Done");
	}


	private static void printFormatedOutput(int itemX, List<ScoredId> topN) {
		System.out.print(itemX);
		for (ScoredId score : topN) {
			System.out.printf(",%d,%.2f", score.getId(), score.getScore());
		}
		System.out.println();
	}


	private static IAssociativiyAlgorithm getAssociativityAlgorithm() {
		// Instantiate an Associativity algorithm
		IAssociativiyAlgorithm algo;

		switch (ALGO) {
		case 1:
			 algo = new AdvancedAssociativity();
			break;

		default:
			algo = new SimpleAssociativity();
			break;
		}
		return algo;
	}

}
