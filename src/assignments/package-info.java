/**
 * This package wraps all the programming assignments that I did during my study of 
 * <b>Coursera Introduction to Recommender Systems course</b>. 
 * Here I will provide a short summary about the assignments that I did and the types of 
 * recommendation system that I have implemented that I implemented as part of the course assignments.
 * All systems are applied to movies recommendations.
 * 
 * Packages explanations:
 * 
 * Dao:
 * 	This package includes some classes provided from the course staff to help with loading data from
 * 		files. I used these classes in some implementations.
 * 
 * Assignment1:
 * 	This included implementing a <b>Non-Personalized Summary-Statistic-based</b> recommendation system.
 * 	The scoring of the movies for recommendation was done based on the associativity between 
 * 		the movies that the user is currently considering and other movies in the system.
 * 	Implementation considered using different equations for computing pairwise movies associativity.
 * 
 * Assignment2:
 * 	This included implementing a <b>Personalized Content-based</b> recommendation system.
 * 	All users' profiles and movies are represented in a Vector Space Model whose dimensions represents
 * 		the content of movies, e.g. tags, actors, genres ... etc. And the movies scoring is based on Cosine
 * 		Similarity between the user's profile and movies in the system.
 * 	Implementation considered using different approaches to build the user profile.
 * 
 * Assignment3:
 * 	This included implementing a <b>Personalized User-User Collaborative-Filtering</b> recommendation system.
 * 	User profile is simply the vector of the ratings that the user gave to any of the movies in the system
 * 		and similarity between users is measured using Cosine Similarity.
 * 	Implementation considered different types of user-neighborhood selection techniques and different
 * 		similarity measuring techniques. 
 * 	  
 */
/**
 * @author Samer Meggaly
 *
 */
package assignments;