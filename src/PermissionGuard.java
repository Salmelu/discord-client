package cz.salmelu.discord;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.Member;
import cz.salmelu.discord.resources.Message;
import cz.salmelu.discord.resources.Role;

/**
 * <p>Simplifies giving server members various permission levels to use commands.</p>
 *
 * <p>Permission guard operates on 2 levels:</p>
 * <ul>
 *     <li>It can disable or enable specific channels, where the module can be used.
 *     This allows your bot to mark specific channels as bot command channels
 *     while still being present in other channels.</li>
 *     <li>It can give your members and roles different permission levels and
 *     set minimum level requirements for each command of your module.</li>
 * </ul>
 * <p>For the exact rule descriptions, check the documentations of methods {@link #isAllowedMember(Message, int)}
 * and {@link #isAllowedChannel(Message)}.</p>
 * <p><b>An example</b>:</p>
 * <pre>
 *      Server server, PermissionGuard guard;
 *      if(!guard.isInitialized()) {
 *          guard.initialize(0);
 *          guard.allowAllChannels(false);
 *          guard.addExcludedChannel(server.getChannelByName("broadcast"));
 *          guard.addExceptionRole(server.getRoleByName("admin"), 500);
 *      }
 *      ...
 *      onMessage() {
 *          if(command.startsWith("add")) {
 *              if(!guard.isAllowed(message, 500)) {
 *                  // Permission is denied
 *                  return;
 *              }
 *          }
 *      }
 * </pre>
 */
public interface PermissionGuard {
    /**
     * <p>Initializes data structures and sets the default permission level to given argument.</p>
     * <p>If the guard was already initialized, this does nothing.</p>
     *
     * @param level default permission level
     */
    void initialize(int level);

    /**
     * Checks if the guard was already initialized.
     * @return true, if {@link #initialize(int)} was called previously, or the data was loaded from previous instance
     */
    boolean isInitialized();

    /**
     * Removes all exception rules set and stored in guard's instance.
     */
    void clearRules();

    /**
     * Changes default permission level set previously by {@link #initialize(int)} to different value.
     * @param level new default permission level
     */
    void changeDefaultLevel(int level);

    /**
     * <p>Creates a new exception for given role.</p>
     * <p>Since the role is associated with a given server, this automatically makes an exception for only
     * a specific server the role is associated with.</p>
     * @param role role with an exception
     * @param level permission level given to members with the role
     */
    void addException(Role role, int level);

    /**
     * <p>Creates a new exception for given member.</p>
     * <p>Since the member is not associated with a specific server, this automatically makes an exception every
     * server the member is in.</p>
     * @param member member with an exception
     * @param level permission level given to the member
     */
    void addException(Member member, int level);

    /**
     * <p>Creates a new exception for given role (specified by its <b>id</b>).</p>
     * <p>Since the role is associated with a given server, this automatically makes an exception for only
     * a specific server the role is associated with.</p>
     * @param id id of the role with an exception
     * @param level permission level given to members with the role
     */
    void addExceptionRole(String id, int level);

    /**
     * <p>Creates a new exception for given member (specified by their <b>id</b>).</p>
     * <p>Since the member is not associated with a specific server, this automatically makes an exception every
     * server the member is in.</p>
     * @param id id of the member with an exception
     * @param level permission level given to the member
     */
    void addExceptionMember(String id, int level);

    /**
     * <p>Gives specified permission level to every private channel.</p>
     * <p>This can be handy to allow some commands with long text output only in private channels,
     * or to allow some basic commands of the module in private channels too.</p>
     * @param level level given to all private messages
     */
    void allowPrivateMessages(int level);

    /**
     * <p>Sets the default behavior for all channels. This includes private channels.</p>
     * <p>If set to <b>true</b>, all <b>channels will pass</b> the permission check except the channels,
     * which have been added to exception set by calling {@link #addExcludedChannel(Channel)} methods.</p>
     * <p>If set to <b>false</b>, all <b>channels will fail</b> the permission check,
     * unless the channel was added to the exception set.</p>
     * <p>Since this <b>applies to private channels</b> too, the desired private channels must be added to
     * exception set too.</p>
     * @param allow desired behavior
     */
    void allowAllChannels(boolean allow);

    /**
     * Removes previously set up exception for a specific role.
     * @param role removed role
     */
    void removeException(Role role);

    /**
     * Removes previously set up exception for a specific member.
     * @param member removed member
     */
    void removeException(Member member);

    /**
     * Removes previously set up exception for a specific role.
     * @param id id of removed role
     */
    void removeExceptionRole(String id);

    /**
     * Removes previously set up exception for a specific member.
     * @param id id of removed member
     */
    void removeExceptionMember(String id);

    /**
     * Adds a channel to the list of exceptions. See {@link #allowAllChannels(boolean)} for description of
     * exception behavior.
     * @param channel channel with exception
     */
    void addExcludedChannel(Channel channel);

    /**
     * Adds a channel to the list of exceptions. See {@link #allowAllChannels(boolean)} for description of
     * exception behavior.
     * @param id id of the channel with exception
     */
    void addExcludedChannel(String id);

    /**
     * Removes a channel from the list of exceptions. See {@link #allowAllChannels(boolean)} for description of
     * exception behavior.
     * @param channel channel with exception
     */
    void removeExcludedChannel(Channel channel);

    /**
     * Removes a channel from the list of exceptions. See {@link #allowAllChannels(boolean)} for description of
     * exception behavior.
     * @param id id of the channel with exception
     */
    void removeExcludedChannel(String id);

    /**
     * <p>Checks if the command is allowed for the author given the level.</p>
     * @param message received message
     * @param level required level
     * @return true if both {@link #isAllowedChannel(Message)} and {@link #isAllowedMember(Message, int)} return true
     */
    boolean isAllowed(Message message, int level);

    /**
     * <p>Checks if the command is allowed in the channel where the message was posted.</p>
     * <p>The message is allowed either if</p>
     * <ul>
     *     <li>{@link #allowAllChannels(boolean)} was set to <b>true</b>
     *     and the channel is <b>not</b> on the exception list</li>
     *     <li>{@link #allowAllChannels(boolean)} was set to <b>false</b>
     *     and the channel is on the exception list</li>
     * </ul>
     * @param message received message
     * @return true if the conditions are fulfilled
     */
    boolean isAllowedChannel(Message message);

    /**
     * <p>Checks if the command is allowed for the user that posted the message.</p>
     * <p>The command is allowed if:</p>
     * <ul>
     *     <li>The message is in private channel and the private channel level
     *     (set by {@link #allowPrivateMessages(int)}) is higher than required level</li>
     *     <li>The author of the message is on the exception list and the level set with the exception is
     *     higher than required level</li>
     *     <li>Any of the member's roles on the server where the message was posted is on the exception list.
     *     Then the role with highest level set by exception is compared to the required level.</li>
     *     <li>If none of the above matched, the command is allowed if the default level is higher than required level.</li>
     * </ul>
     * @param message received message
     * @param level minimum required level
     * @return true if the command is allowed and should be processed
     */
    boolean isAllowedMember(Message message, int level);
}
