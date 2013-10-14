package assignments.assignment3;

import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.HashMap;
import java.util.Set;

import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

import com.google.common.base.Predicate;

public class UserUserCollaborativeFilteringNeighbors {
	
	private final VectorSimilarity vecSim;
	private final UserEventDAO userEventDAO;
	private final UserDAO userDAO;
	private HashMap<Long, SparseVector> meanCenteredRatings;

	public UserUserCollaborativeFilteringNeighbors(VectorSimilarity vecSim,
			UserDAO userDAO, UserEventDAO userEventDAO) {
		
		this.vecSim = vecSim;
		this.userEventDAO = userEventDAO;
		this.userDAO = userDAO;
		computeSystemUsersMeanCenteredRatings();
	}

	public Long2DoubleMap getUserNNeighbors(long user, long item, int N) {
		
		Set<Long> users = meanCenteredRatings.keySet();
		assert users.contains(user) == true;
		SparseVector userMean = (meanCenteredRatings.get(user)).immutable();
		HashMap<Long, Double> usersSims = new HashMap<Long, Double>(users.size());
		
		for (long ui : users) {
			if (ui != user) {
				UserHistory<Rating> uiHistory = userEventDAO.getEventsForUser(ui, Rating.class);
				uiHistory = uiHistory.filter(new IsItemPredicate(item));
				if (!uiHistory.isEmpty()) {
					double sim = vecSim.similarity(userMean, meanCenteredRatings.get(ui));
					usersSims.put(ui, sim);
				}
			}
		}
		MutableSparseVector msv = MutableSparseVector.create(usersSims);
		LongList sortedNKeys = (msv.keysByValue(true)).subList(0, N);
		
		Long2DoubleMap neighbors = new Long2DoubleArrayMap(sortedNKeys.size());
		for (long ui : sortedNKeys) {
			neighbors.put(ui, msv.get(ui));
		}
		return neighbors;
	}
	
	private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userEventDAO.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }

	private void computeSystemUsersMeanCenteredRatings() {
		if (meanCenteredRatings == null) {
    		// find the set of all users in the system
        	LongSet users = userDAO.getUserIds();
        	meanCenteredRatings = new HashMap<Long, SparseVector>(users.size());
    		
        	for (long uid : users) {
        		// get the rating of the current user
        		SparseVector userRatings = getUserRatingVector(uid);
				meanCenteredRatings.put(uid, new MeanCenteringTransformation(
						userRatings.mean()).apply(userRatings.mutableCopy()));
        	}
    	}
	}

	class MeanCenteringTransformation implements VectorTransformation {
		private final double mean;
		
		public MeanCenteringTransformation(double mean) {
			this.mean = mean;
		}

		@Override
		public MutableSparseVector apply(MutableSparseVector vector) {
			vector.add(-mean);
			return vector;
		}

		@Override
		public MutableSparseVector unapply(MutableSparseVector vector) {
			vector.add(mean);
			return vector;
		}
	}
	
	class IsItemPredicate implements Predicate<Rating> {
		
		private final long item;
		public IsItemPredicate(long item) {
			this.item = item;
		}

		@Override
		public boolean apply(Rating r) {
			return r.getItemId() == item;
		}
		
	}
}
