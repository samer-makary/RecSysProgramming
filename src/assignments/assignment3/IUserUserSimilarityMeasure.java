package assignments.assignment3;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * This interface wraps the basic methods needed for computing the similarity
 * between two users.
 * 
 * @author Samer Meggaly
 * 
 */
public interface IUserUserSimilarityMeasure {

	public PriorityQueue<Entry> getSimilarity(long user, long otherUser);
	
	public PriorityQueue<Entry> getSimilarity(long user, LongList otherUsers);
	
}
