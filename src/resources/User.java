package cz.salmelu.discord.resources;

/**
 * A single Discord user.
 */
public interface User {
    /**
     * Gets user's unique id given by Discord.
     * @return unique id
     */
    String getId();

    /**
     * Gets user's account name. The user's name is not unique and in case multiple users use the same name,
     * they have a different discriminator.
     * @return user's name
     */
    String getName();

    /**
     * Gets the discriminator. The discriminator is a 4 digit number attached to user's name to distinguish
     * different users using the same name.
     * @return user's discriminator
     */
    String getDiscriminator();

    /**
     * <p>Gets a mention string for the user.</p>
     * <p>This converts user's name and discriminator into a message specific string, which triggers a mention.</p>
     * <p>Use this string in a message if you wish to mention the user.</p>
     * @return a string for mentioning the user
     */
    String getMention();

    /**
     * <p>Opens a new private channel for sending direct messages to the user.</p>
     * <p>This method sends a request to Discord server and therefore it blocks until it's completed.</p>
     * @return an instance of private channel
     */
    PrivateChannel createPrivateChannel();
}
