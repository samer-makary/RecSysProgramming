package assignments.assignment2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.scored.ScoredId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import assignments.dao.CSVItemTagDAO;
import assignments.dao.MOOCRatingDAO;
import assignments.dao.MOOCUserDAO;
import assignments.dao.RatingFile;
import assignments.dao.TagFile;
import assignments.dao.TitleFile;
import assignments.dao.UserFile;

/**
 * Simple hello-world program.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ContentBasedRecommenderMain {
    private static final Logger logger = LoggerFactory.getLogger(ContentBasedRecommenderMain.class);
    private static String dataFile = "./data/assignment2/";
	private static final int ALGO = 2;
	private static final int N = 5;
	private static final double POS_RATING_THRESHOLD = 3.5;
	
	private static String ratingsFile;
	private static String moviesFile;
	private static String usersFile;
	private static String tagsFile;
	

    public static void main(String[] args) throws RecommenderBuildException {
    	// Set data files
    	ratingsFile = dataFile + "ratings.csv";
    	moviesFile = dataFile + "movie-titles.csv";
    	usersFile = dataFile + "users.csv";
    	tagsFile = dataFile + "movie-tags.csv";
   	
        LenskitConfiguration config = configureRecommender();

        logger.info("building recommender");
        Recommender rec = LenskitRecommender.build(config);

        if (args.length == 0) {
			args = new String[] { "4045", "144", "3855", "1637", "2919",
					"4028", "778", "4263", "3771", "1065" };
			logger.info("No user IDs specified; program will use default values\n\t>>> "
					+ Arrays.asList(args).toString());
        }

        // we automatically get a useful recommender since we have a scorer
        ItemRecommender irec = rec.getItemRecommender();
        assert irec != null;
        try {
            // Generate 5 recommendations for each user
            for (String user: args) {
                long uid;
                try {
                    uid = Long.parseLong(user);
                } catch (NumberFormatException e) {
                    logger.error("cannot parse user {}", user);
                    continue;
                }
                List<ScoredId> recs = irec.recommend(uid, N);
                if (recs.isEmpty()) {
                    logger.warn("no recommendations for user {}, do they exist?", uid);
                }
                System.out.format("recommendations for user %d:\n", uid);
                for (ScoredId id: recs) {
                    System.out.format("  %d: %.4f\n", id.getId(), id.getScore());
                }
            }
        } catch (UnsupportedOperationException e) {
            if (e.getMessage().equals("stub implementation")) {
                System.out.println("Congratulations, the stub builds and runs!");
            }
        }
    }

    /**
     * Create the LensKit recommender configuration.
     * @return The LensKit recommender configuration.
     */
    // LensKit configuration API generates some unchecked warnings, turn them off
    @SuppressWarnings("unchecked")
    private static LenskitConfiguration configureRecommender() {
        LenskitConfiguration config = new LenskitConfiguration();
        // configure the rating data source
        config.bind(EventDAO.class)
              .to(MOOCRatingDAO.class);
        config.set(RatingFile.class)
              .to(new File(ratingsFile));

        // use custom item and user DAOs
        // specify item DAO implementation with tags
        config.bind(ItemDAO.class)
              .to(CSVItemTagDAO.class);
        // specify tag file
        config.set(TagFile.class)
              .to(new File(tagsFile));
        // and title file
        config.set(TitleFile.class)
              .to(new File(moviesFile));

        // our user DAO can look up by user name
        config.bind(UserDAO.class)
              .to(MOOCUserDAO.class);
        config.set(UserFile.class)
              .to(new File(usersFile));

        // use the TF-IDF scorer you will implement to score items
        config.bind(ItemScorer.class)
              .to(ContentBasedTFIDFScorer.class);
        // set the user profile accumulating algorithm
        switch (ALGO) {
		case 1:
			config.set(UserProfileType.class)
				  .to(new UserProfileUnweightedAlgorithm(POS_RATING_THRESHOLD));
			break;
			
		case 2:
			config.set(UserProfileType.class)
				  .to(new UserProfileWeightedAlgorithm(0.0));
			break;
			
		default:
			throw new IllegalArgumentException("Unknown User profile algorithm type: " + ALGO);
		}
        return config;
    }
}