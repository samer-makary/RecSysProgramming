package assignments.assignment3;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * This interface defines the basic methods that are needed for computing the
 * scoring (rating) equation between a user and an item.
 * 
 * @author Samer Meggaly
 * 
 */
public interface IScoringEquation {

	/**
	 * The function computes the value of the rating equation for a given user
	 * and item. The rating is used for user-user collaborative filtering, so
	 * the user's neighborhood is also need for the computations.
	 * 
	 * @param user
	 *            The user for which the item needs to be rated
	 * @param item
	 *            The item that needs to be rated
	 * @param userNeighborhood
	 *            List of userIDs of the users in the neighborhood and their
	 *            corresponding relatedness to the given user
	 * @return
	 */
	public double rate(long user, long item, Long2DoubleMap userNeighborhood);
}
