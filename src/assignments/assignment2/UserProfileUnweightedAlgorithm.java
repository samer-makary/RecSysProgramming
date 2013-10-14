package assignments.assignment2;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class UserProfileUnweightedAlgorithm implements IUserProfileType {
	
	private double positiveRatingThreshold;
	
	public UserProfileUnweightedAlgorithm( double positiveRatingThreshold) {
		this.positiveRatingThreshold = positiveRatingThreshold;
	}

	@Override
	public void updateUserProfile(MutableSparseVector userProfile,
			ContentBasedDataModel model, Rating rating, double userRatingsMean) {
		
		// In LensKit, ratings are expressions of preference
        Preference p = rating.getPreference();
        // We'll never have a null preference. But in LensKit, ratings can have null
        // preferences to express the user rating an item
        if (p != null && p.getValue() >= positiveRatingThreshold) {
            // The user likes this item!
            // TODO Get the item's vector and add it to the user's profile
        	MutableSparseVector itemVector = model.getItemVector(rating.getItemId()).mutableCopy(); 
        	userProfile.add(itemVector);
        }
	}

}
