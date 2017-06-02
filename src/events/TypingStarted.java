package cz.salmelu.discord.events;

import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.User;

public interface TypingStarted {
    User getUser();
    Channel getChannel();
    long getTimestamp();
}
