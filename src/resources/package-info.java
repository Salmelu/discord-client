/**
 * <p>Abstractions of Discord resources.</p>
 * <p>Classes in this package represent a single Discord entities, which can be acted upon.
 * The instances of these entities will be given to the application either by method calls on these entities,
 * or as arguments in your listeners methods in classes from {@link cz.salmelu.discord.listeners} package.
 * Do not attempt to create these instances alone as it might either desynchronize the client from the server state,
 * or it might trigger unexpected behavior.</p>
 */
package cz.salmelu.discord.resources;