/**
 * 
 */
package de.torui.CoflCore.CoflCore.configuration;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
/**
 * @author Florian Rinke
 *
 */
public @interface Description {
	public String value();
}
