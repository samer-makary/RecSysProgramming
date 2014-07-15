package assignments.assignment6;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import assignments.dao.*;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDMain {
    private static final Logger logger = LoggerFactory.getLogger("svd-assignment");
    private static final Pattern USER_ITEM_PAT = Pattern.compile("(\\d+):(\\d+)");

    /**
     * Main entry point to the program.
     * @param args The <tt>user:item</tt> pairs to score.
     */
    public static void main(String[] args) {
    	
		String submitArgs = "1184:604 1184:854 1184:9741 1184:752 1184:197 "
				+ "84:280 84:10020 84:2164 84:9741 84:585 "
				+ "4732:2024 4732:146 4732:274 4732:2164 4732:641 "
				+ "3495:134 3495:8467 3495:581 3495:141 3495:1637 "
				+ "2679:161 2679:1422 2679:664 2679:5503 2679:243";
    	// testing arguments to mimic the command line arguments
//		String testArgs = "1024:77 1024:268 1024:462 1024:393 1024:36955 2048:77 2048:36955 2048:788";
//		String debugArgs = "1024:77";
    	
//		String tmp = debugArgs;
//		String tmp = testArgs;
		String tmp = submitArgs;
    	
		String argsForUserItemMean = "--user-item-mean " + tmp;
    	String argsForUserMean = "--user-mean " + tmp;
    	String argsForItemMean = "--item-mean " + tmp;
    	String argsForGlobalMean = "--global-mean " + tmp;
		String[] allArgs = new String[] { argsForUserItemMean, argsForUserMean,
				argsForItemMean, argsForGlobalMean };
		
		if (args.length == 0) {	// if no command line arguments are passed use the testing ones
			StringBuilder sb = new StringBuilder();
			for (String s: allArgs) {
				args = s.split(" ");
				SVDMain program = initialize(args);
				sb.append(program.run());
			}
			System.out.println(sb.toString());
		} else {
			SVDMain program = initialize(args);
			System.out.println(program.run());
		}
    }

    /**
     * Parse arguments and set up an SVD runner.
     * @param args The command line arguments.
     * @return The SVD program, configured and ready to run.
     */
    public static SVDMain initialize(String[] args) {
        BaselineMode baselineMode = BaselineMode.GLOBAL_MEAN;
        Map<Long,Set<Long>> toScore = Maps.newHashMap();
        for (String arg: args) {
            logger.debug("parsing argument: {}", arg);
            if (arg.equals("--global-mean")) {
                baselineMode = BaselineMode.GLOBAL_MEAN;
            } else if (arg.equals("--user-mean")) {
                baselineMode = BaselineMode.USER_MEAN;
            } else if (arg.equals("--item-mean")) {
                baselineMode = BaselineMode.ITEM_MEAN;
            } else if (arg.equals("--user-item-mean")) {
                baselineMode = BaselineMode.USER_ITEM_MEAN;
            } else if (arg.equals("--all")) {
                toScore = null;
            } else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("unknown flag " + arg);
            } else {
                Matcher m = USER_ITEM_PAT.matcher(arg);
                if (m.matches()) {
                    long uid = Long.parseLong(m.group(1));
                    long iid = Long.parseLong(m.group(2));
                    if (!toScore.containsKey(uid)) {
                        toScore.put(uid, Sets.<Long>newHashSet());
                    }
                    toScore.get(uid).add(iid);
                } else {
                    throw new IllegalArgumentException("unparseable argument " + arg);
                }
            }
        }
        return new SVDMain(baselineMode, toScore);
    }

    BaselineMode baselineMode;
    Map<Long,Set<Long>> toScore;

    /**
     * Construct a new SVD program.
     * @param base The baseline mode.
     * @param requests The items to score for each user.
     */
    public SVDMain(BaselineMode base, Map<Long,Set<Long>> requests) {
        baselineMode = base;
        toScore = requests;
    }

    /**
     * Create the LensKit recommender configuration.
     * @return The LensKit recommender configuration.
     */
    // LensKit configuration API generates some unchecked warnings, turn them off
    @SuppressWarnings("unchecked")
    private LenskitConfiguration configureRecommender() {
        LenskitConfiguration config = new LenskitConfiguration();
        // configure the rating data source
        config.bind(EventDAO.class)
              .to(MOOCRatingDAO.class);
        config.set(RatingFile.class)
              .to(new File("data/assignment6/ratings.csv"));

        // use custom item and user DAOs
        // our item DAO has title information
        config.bind(ItemDAO.class)
              .to(MOOCItemDAO.class);
        config.addRoot(UserDAO.class);
        // and title file
        config.set(TitleFile.class)
              .to(new File("data/assignment6/movie-titles.csv"));

        // our user DAO can look up by user name
        config.bind(UserDAO.class)
              .to(MOOCUserDAO.class);
        config.addRoot(UserDAO.class);
        config.set(UserFile.class)
              .to(new File("data/assignment6/users.csv"));

        // use the item-item scorer you will implement to score items
        config.bind(ItemScorer.class)
              .to(SVDItemScorer.class);
        baselineMode.configure(config);
        config.set(LatentFeatureCount.class)
              .to(10);
        return config;
    }

    public String run() {
        LenskitConfiguration config = configureRecommender();
        LenskitRecommender rec;
        try {
            rec = LenskitRecommender.build(config);
        } catch (RecommenderBuildException e) {
            logger.error("error building recommender", e);
            System.exit(2);
            throw new AssertionError(); // to de-confuse unreachable code detection
        }

        // Get the item title DAO, so we can look up movie titles
        ItemTitleDAO titleDAO = rec.get(ItemTitleDAO.class);

        // Get the item scorer and go!
        ItemScorer scorer = rec.getItemScorer();
        assert scorer != null;

        if (toScore == null) {
            logger.debug("loading user/item sets");
            UserDAO userDAO = rec.get(UserDAO.class);
            if (userDAO == null) {
                logger.error("no user DAO");
                System.exit(2);
            }
            toScore = Maps.newHashMap();
            for (Long user: userDAO.getUserIds()) {
                toScore.put(user, titleDAO.getItemIds());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n--------------------------------BEG OUTPUT--------------------------------\n");
        logger.info("scoring for {} users", toScore.size());
        sb.append(String.format	("scoring for %d users\n", toScore.size()));
        for (Map.Entry<Long,Set<Long>> scoreRequest: toScore.entrySet()) {
            long user = scoreRequest.getKey();
            Set<Long> items = scoreRequest.getValue();
            logger.info("scoring {} items for user {}", items.size(), user);
            sb.append(String.format("scoring %d items for user %d\n", items.size(), user));
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
                sb.append(String.format("%d,%d,%s,%s\n", user, item, score, title));
            }
        }
        sb.append("\n--------------------------------END OUTPUT--------------------------------\n");
        return sb.toString();
    }
}
