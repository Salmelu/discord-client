package cz.salmelu.discord.resources;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface Message {
    String getId();
    String getRawText();
    String getText();

    Channel getChannel();

    Collection<Reaction> getReactions();
    void addReaction(Emoji emoji);
    void removeReaction(Emoji emoji);
    List<User> getReactions(Emoji emoji);

    User getAuthor();
    LocalDateTime getSentTime();
    LocalDateTime getEditedTime();
    boolean isTTS();

    boolean isMentionAtEveryone();
    List<User> getMentionedUsers();
    List<Role> getMentionedRoles();

    void reply(String reply);

}
