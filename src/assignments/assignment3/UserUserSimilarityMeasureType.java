package assignments.assignment3;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

/**
 * Parameter annotation for user-user collaborative filtering neighborhood selection
 * @author Samer Meggaly
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Qualifier
@Parameter(VectorSimilarity.class)
public @interface UserUserSimilarityMeasureType {

}