package assignments.assignment3;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.UserHistory;

import com.google.common.base.Predicate;

public class UserUserCollaborativeFilteringNeighbors {
	
	private final IUserUserSimilarityMeasure vecSim;
	private final UserEventDAO userEventDAO;
	private final UserDAO userDAO;

	public UserUserCollaborativeFilteringNeighbors(
			@UserUserSimilarityMeasureType IUserUserSimilarityMeasure vecSim,
			UserDAO userDAO, UserEventDAO userEventDAO) {

		this.vecSim = vecSim;
		this.userEventDAO = userEventDAO;
		this.userDAO = userDAO;
	}

	public Long2DoubleMap getUserNNeighbors(long user, long item, int N) {

		LongSet systemUsers = userDAO.getUserIds();
		LongList usersRatedItem = new LongArrayList();
		for (long ui : systemUsers) {
			if (ui != user) {
				UserHistory<Rating> uiHistory = userEventDAO.getEventsForUser(
						ui, Rating.class);
				uiHistory = uiHistory.filter(new IsItemPredicate(item));
				if (!uiHistory.isEmpty()) {
					usersRatedItem.add(ui);
				}
			}
		}
		PriorityQueue<Entry> cachedUserSimilarities = vecSim.getSimilarity(user, usersRatedItem);

		Long2DoubleMap neighbors = new Long2DoubleArrayMap(N);
		boolean foundNUser = false;
		while (!foundNUser && !cachedUserSimilarities.isEmpty()) {
			Entry e = cachedUserSimilarities.dequeue();
			neighbors.put(e.getLongKey(), e.getDoubleValue());
			foundNUser = neighbors.size() == N;
		}
		
		return neighbors;
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
