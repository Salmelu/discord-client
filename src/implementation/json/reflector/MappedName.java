package cz.salmelu.discord.implementation.json.reflector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotatiton for enums.</p>
 * <p>Allows enums having 2 different names, one in code and other in serialized form.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MappedName {
    /**
     * Serialized name for enum
     */
    String value();
}
