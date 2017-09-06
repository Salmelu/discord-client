/**
 * <p>Contains required objects used for communication with Discord servers.</p>
 * <p>This package contains 4 subpackages:</p>
 * <ul>
 *     <li>{@link cz.salmelu.discord.implementation.json.reflector} - special classes responsible for
 *     serialization and deserialization of JSON objects and their Java mirrors.</li>
 *     <li>{@link cz.salmelu.discord.implementation.json.request} - Request POJOs for Gateway</li>
 *     <li>{@link cz.salmelu.discord.implementation.json.resources} - POJOs representing Discord resources</li>
 *     <li>{@link cz.salmelu.discord.implementation.json.response} - POJOs for Gateway events</li>
 * </ul>
 * <p>All the POJOs are not documented as they have no special functions. Those classes are used as
 * a data holder for more complex objects, located in {@link cz.salmelu.discord.implementation.resources}.</p>
 */
package cz.salmelu.discord.implementation.json;