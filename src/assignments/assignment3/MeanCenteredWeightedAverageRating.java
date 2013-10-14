package assignments.assignment3;

import javax.inject.Inject;

import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.SparseVector;

public class MeanCenteredWeightedAverageRating implements IRatingEquation {
	
	private final UserEventDAO userEventDAO;
	private Long2DoubleMap usersMeanRating;
	
	@Inject
	public MeanCenteredWeightedAverageRating(UserEventDAO userEventDAO,
			UserDAO userDAO) {
		this.userEventDAO = userEventDAO;
		computeSystemUsersMeanRating(userDAO);
	}
	
	@Override
	public double rate(long user, long item, Long2DoubleMap userNeighborhood) {
		double sumWeightedRatings = 0.0;
		double sumAbsWeights = 0.0;
		for (long uid : userNeighborhood.keySet()) {
			// only consider the user if the user rated the item
			SparseVector userRatings = getUserRatingVector(uid);
			if (userRatings.containsKey(item)) {
				double centeredRating = userRatings.get(item) - usersMeanRating.get(uid);
				sumWeightedRatings += userNeighborhood.get(uid) * centeredRating;
				sumAbsWeights += Math.abs(userNeighborhood.get(uid));
			}
		}

		if (Double.compare(sumAbsWeights, 0) == 0) {
			sumAbsWeights = 1E-15;
		}
		return (sumWeightedRatings / sumAbsWeights) + usersMeanRating.get(user);
	}
	
	/**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The rating vector.
     */
    private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userEventDAO.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }
    
    private void computeSystemUsersMeanRating(UserDAO userDAO) {
    	if (usersMeanRating == null) {
    		// find the set of all users in the system
        	LongSet users = userDAO.getUserIds();
        	usersMeanRating = new Long2DoubleArrayMap(users.size());
    		
        	for (long uid : users) {
        		// get the rating of the current user
        		SparseVector userRatings = getUserRatingVector(uid);
        		usersMeanRating.put(uid, userRatings.mean());
        	}
    	}
    }

}
