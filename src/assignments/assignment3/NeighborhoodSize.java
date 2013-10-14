package assignments.assignment3;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.grouplens.lenskit.core.Parameter;

/**
 * This annotation is just used for binding the neighborhood size. 
 * @author Samer Meggaly
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Qualifier
@Parameter(Integer.class)
public @interface NeighborhoodSize {
}
