package assignments.assignment2;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ContentBasedTFIDFScorer extends AbstractItemScorer {
    private final UserEventDAO dao;
    private final ContentBasedDataModel model;
    private final IUserProfileType userProfileType;

    /**
	 * Construct a new item scorer. LensKit's dependency injector will call this
	 * constructor and provide the appropriate parameters.
	 * 
	 * @param dao
	 *            The user-event DAO, so we can fetch a user's ratings when
	 *            scoring items for them.
	 * @param m
	 *            The pre-computed model containing the item tag vectors.
	 * @param up
	 *            The User profile type, it will be used to accumulate updates
	 *            to User profile vector according to the User ratings.
	 */
    @Inject
	public ContentBasedTFIDFScorer(UserEventDAO dao, ContentBasedDataModel m,
			@UserProfileType IUserProfileType up) {
        this.dao = dao;
        this.model = m;
        this.userProfileType = up;
    }

    /**
     * Generate item scores personalized for a particular user.  For the TFIDF scorer, this will
     * prepare a user profile and compare it to item tag vectors to produce the score.
     *
     * @param user   The user to score for.
     * @param output The output vector.  The contract of this method is that the caller creates a
     *               vector whose possible keys are all items that should be scored; this method
     *               fills in the scores.
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector output) {
        // Get the user's profile, which is a vector with their 'like' for each tag
        SparseVector userVector = makeUserVector(user);

        // Loop over each item requested and score it.
        // The *domain* of the output vector is the items that we are to score.
        for (VectorEntry e: output.fast(VectorEntry.State.EITHER)) {
            // Score the item represented by 'e'.
            // Get the item vector for this item
            SparseVector iv = model.getItemVector(e.getKey());
            // TODO Compute the cosine of this item and the user's profile, store it in the output vector
            double score = 0.0;
            score = (userVector.dot(iv)) / (userVector.norm() * iv.norm());
            output.set(e, score);
        }
    }

    private SparseVector makeUserVector(long user) {
        // Get the user's ratings
        List<Rating> userRatings = dao.getEventsForUser(user, Rating.class);
        if (userRatings == null) {
            // the user doesn't exist
            return SparseVector.empty();
        }

        // Create a new vector over tags to accumulate the user profile
        MutableSparseVector profile = model.newTagVector();
        // Fill it with 0's initially - they don't like anything
        profile.fill(0);
        
        // Compute the mean of the ratings
        double ratingsMean = 0.0;
        for (Rating r : userRatings)
        	ratingsMean += r.getPreference().getValue();
        ratingsMean /= userRatings.size();

        // Iterate over the user's ratings to build their profile
        for (Rating r: userRatings) {
        	// This object will update the user profile vector according to the need algorithm.
        	// Strategy Design Pattern ;) 
        	// -- Samer Meggaly
            userProfileType.updateUserProfile(profile, model, r, ratingsMean);
        }

        // The profile is accumulated, return it.
        // It is good practice to return a frozen vector.
        return profile.freeze();
    }
}
