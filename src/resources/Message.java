package cz.salmelu.discord.resources;

import java.time.LocalDateTime;
import java.util.List;

public interface Message {
    String getId();
    String getRawText();
    Channel getChannel();

    User getAuthor();
    LocalDateTime getSentTime();
    LocalDateTime getEditedTime();
    boolean isTTS();

    boolean isMentionAtEveryone();
    List<User> getMentionedUsers();
    List<Role> getMentionedRoles();
}
