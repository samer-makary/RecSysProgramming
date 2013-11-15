package assignments.assignment3;

import javax.inject.Inject;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;

import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

public class BasicUUSimilarity implements
		IUserUserSimilarityMeasure {
	
	private final VectorSimilarity vecSim;
	private final UserEventDAO userEventDAO;
	protected Long cachedUser;
	protected SparseVector cachedUserRatings;
	protected Long2DoubleMap cachedUserSimilarities;
	
	@Inject
	public BasicUUSimilarity(VectorSimilarity vecSim, UserEventDAO userEventDAO) {
		this.vecSim = vecSim;
		this.userEventDAO = userEventDAO;
	}
	
	public VectorSimilarity getUserVectorSimilarity() {
		return vecSim;
	}
	
	protected SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userEventDAO.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }
	
	protected void ensureUsersSimilaritiesAreCached(long user, LongList otherUsers) {
		if (cachedUser == null || cachedUser != user) {
    		cachedUser = user;
			cachedUserRatings = getUserRatingVector(user);
    		cachedUserSimilarities = new Long2DoubleArrayMap(otherUsers.size());
		}
		
		for (long uid : otherUsers) {
			// check if user's mean-centered rating vector is cached before
			if (!cachedUserSimilarities.containsKey(uid)) {
				double sim = vecSim.similarity(cachedUserRatings, getUserRatingVector(uid));
				cachedUserSimilarities.put(uid, sim);
			}
		}
	}
	
	@Override
	public PriorityQueue<Entry> getSimilarity(long user, long otherUser) {
		return getSimilarity(user, LongLists.singleton(otherUser));
	}

	@Override
	public PriorityQueue<Entry> getSimilarity(long user, LongList otherUsers) {
		// make sure users mean rating is found computed
		ensureUsersSimilaritiesAreCached(user, otherUsers);

		// compute similarities
		PriorityQueue<Entry> usersSims = new ObjectHeapPriorityQueue<Entry>(
				otherUsers.size());
		for (long uid : otherUsers) {
			double sim = cachedUserSimilarities.get(uid);
			usersSims.enqueue(new PQEntry(uid, sim));
		}
		return usersSims;
	}
	
	
	private class PQEntry implements Entry, Comparable<PQEntry> {
		
		private final Long key;
		private Double value;
		
		public PQEntry(long key, double value) {
			this.key = new Long(key);
			this.value = new Double(value);
		}
		
		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public Double getValue() {
			return value;
		}

		@Override
		public Double setValue(Double arg0) {
			Double temp = value;
			value = arg0;
			return temp;
		}

		@Override
		public double getDoubleValue() {
			return value.doubleValue();
		}

		@Override
		public long getLongKey() {
			return key.longValue();
		}

		@Override
		public double setValue(double arg0) {
			double temp = value.doubleValue();
			value = new Double(arg0);
			return temp;
		}

		@Override
		public int compareTo(PQEntry o) {
			return -Double.compare(this.value, o.value);
		}
		
	}

}
