package assignments.assignment3;

import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.inject.Inject;

import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

public class MeanCenteredUUSimilarity extends BasicUUSimilarity {

	private Long2ObjectMap<SparseVector> meanUsersRating;

	@Inject
	public MeanCenteredUUSimilarity(VectorSimilarity vecSim,
			UserDAO userDAO, UserEventDAO userEventDAO) {
		
		super(vecSim, userEventDAO);
		this.cachedUser = null;
		computeSystemUsersMean(userDAO);
		
	}

	private void computeSystemUsersMean(UserDAO userDAO) {
		LongSet systemUsers = userDAO.getUserIds();
		meanUsersRating = new Long2ObjectArrayMap<SparseVector>(systemUsers.size());
		for (long uid : systemUsers) {
			// get the rating of the current user
			SparseVector userRatings = getUserRatingVector(uid);
			userRatings = new MeanCenteringTransformation(
					userRatings.mean()).apply(userRatings.mutableCopy());
			meanUsersRating.put(uid, userRatings);
		}
	}

	@Override
	protected void ensureUsersSimilaritiesAreCached(long user, LongList otherUsers) {
		if (cachedUser == null || cachedUser != user) {
    		cachedUser = user;
			cachedUserRatings = meanUsersRating.get(user);
    		cachedUserSimilarities = new Long2DoubleArrayMap(otherUsers.size());
		}
		
		VectorSimilarity vecSim = getUserVectorSimilarity();
		for (long uid : otherUsers) {
			// check if user's mean-centered rating vector is cached before
			if (!cachedUserSimilarities.containsKey(uid)) {
				double sim = vecSim.similarity(cachedUserRatings, meanUsersRating.get(uid));
				cachedUserSimilarities.put(uid, sim);
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
	

}
