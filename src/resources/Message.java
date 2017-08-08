package cz.salmelu.discord.resources;

import cz.salmelu.discord.Emoji;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface Message {
    String getId();
    String getRawText();
    String getText();

    void edit(String newText);

    void delete();

    Channel getChannel();

    Collection<Reaction> getReactions();
    void addReaction(Emoji emoji);
    void removeReaction(Emoji emoji);

    void removeUserReaction(Emoji emoji, User user);

    void removeAllReactions();

    List<User> getReactions(Emoji emoji);

    User getAuthor();
    OffsetDateTime getSentTime();
    OffsetDateTime getEditedTime();
    boolean isTTS();

    boolean isMentionAtEveryone();
    List<User> getMentionedUsers();
    List<Role> getMentionedRoles();

    void reply(String reply);

}
