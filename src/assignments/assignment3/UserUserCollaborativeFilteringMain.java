package assignments.assignment3;

import assignments.dao.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for running the user-user assignment.  Each command line argument is a user:item
 * pair to score.
 *
 * <ul>
 * <li>Revision 3: make compatible with Java 6</li>
 * <li>Revision 2: use root locale for output formatting</li>
 * </ul>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserUserCollaborativeFilteringMain {
    private static final Logger logger = LoggerFactory.getLogger("uu-assignment");
    private static final String ASS_3_DATA_PATH = "./data/assignment3/";
    
    private static final String RATINGS = ASS_3_DATA_PATH + "ratings.csv";
    private static final String MOVIES_TITLES = ASS_3_DATA_PATH + "movie-titles.csv";
    private static final String USERS = ASS_3_DATA_PATH + "users.csv";

    /**
     * Main entry point to the program.
     * @param args The <tt>user:item</tt> pairs to score.
     */
    public static void main(String[] args) {
    	
		args = new String[] { "1024:77", "1024:268", "1024:462", "1024:393",
				"1024:36955", "2048:77", "2048:36955", "2048:788" };
		
    	
        Map<Long,Set<Long>> toScore = parseArgs(args);

        LenskitConfiguration config = configureRecommender();
        LenskitRecommender rec;
        try {
            rec = LenskitRecommender.build(config);
        } catch (RecommenderBuildException e) {
            logger.error("error building recommender", e);
            System.exit(2);
            throw new AssertionError(); // to de-confuse unreachable code detection
        }

        // Get the item scorer and go!
        ItemScorer scorer = rec.getItemScorer();
        assert scorer != null;
        // Also get the item title DAO, so we can look up movie titles
        ItemTitleDAO titleDAO = rec.get(ItemTitleDAO.class);

        for (Map.Entry<Long,Set<Long>> scoreRequest: toScore.entrySet()) {
            long user = scoreRequest.getKey();
            Set<Long> items = scoreRequest.getValue();
            logger.info("scoring {} items for user {}", items.size(), user);
            // We call the score method that takes a set of items.
            // AbstractItemScorer delegates this method to the one you are supposed to implement.
            SparseVector scores = scorer.score(user, items);
            for (long item: items) {
                String score;
                if (scores.containsKey(item)) {
                    score = String.format(Locale.ROOT, "%.4f", scores.get(item));
                } else {
                    score = "NA";
                }
                String title = titleDAO.getItemTitle(item);
                System.out.format("%d,%d,%s,%s\n", user, item, score, title);
            }
        }
    }

    /**
     * Parse the command line arguments.
     * @param args The command line arguments.
     * @return A map of users to the sets of items to score for them.
     */
    private static Map<Long, Set<Long>> parseArgs(String[] args) {
        Pattern pat = Pattern.compile("(\\d+):(\\d+)");
        Map<Long, Set<Long>> map = Maps.newHashMap();
        for (String arg: args) {
            Matcher m = pat.matcher(arg);
            if (m.matches()) {
                long uid = Long.parseLong(m.group(1));
                long iid = Long.parseLong(m.group(2));
                if (!map.containsKey(uid)) {
                    map.put(uid, Sets.<Long>newHashSet());
                }
                map.get(uid).add(iid);
            } else {
                logger.error("unparseable command line argument {}", arg);
            }
        }
        return map;
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
              .to(new File(UserUserCollaborativeFilteringMain.RATINGS));

        // use custom item and user DAOs
        // our item DAO has title information
        config.bind(ItemDAO.class)
              .to(MOOCItemDAO.class);
        // and title file
        config.set(TitleFile.class)
              .to(new File(UserUserCollaborativeFilteringMain.MOVIES_TITLES));

        // our user DAO can look up by user name
        config.bind(UserDAO.class)
              .to(MOOCUserDAO.class);
        config.set(UserFile.class)
              .to(new File(UserUserCollaborativeFilteringMain.USERS));
        
        // use the TF-IDF scorer you will implement to score items
        config.bind(ItemScorer.class)
              .to(SimpleUserUserItemScorer.class);
        config.set(RatingEquationType.class)
        	  .to(MeanCenteredWeightedAverageRating.class);
        config.set(UserUserSimilarityMeasureType.class)
        	  .to(new CosineVectorSimilarity());
        config.set(NeighborhoodSize.class)
        	  .to(new Integer(30));
        
        return config;
    }
}