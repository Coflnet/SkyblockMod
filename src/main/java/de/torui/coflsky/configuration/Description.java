/**
 * 
 */
package de.torui.coflsky.configuration;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Florian Rinke
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Description {
	String value();
}
