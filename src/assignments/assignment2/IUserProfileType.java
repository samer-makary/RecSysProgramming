package assignments.assignment2;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public interface IUserProfileType {
	
	/**
	 * This method updates the User profile vector given the data model and the
	 * rating that is supposed to affect the User vector.
	 * 
	 * @param userProfile Current vector of the User's profile which should be updated.
	 * @param model	Data model to be able to retrieve the vector of the Item that the User rated.
	 * @param rating Rating object the User, represented here by the profile vector, gave to a certain Item.
	 * @param userRatingsMean The average mean of all the ratings of the user.
	 */
	public void updateUserProfile(MutableSparseVector userProfile,
			ContentBasedDataModel model, Rating rating, double userRatingsMean);
}
