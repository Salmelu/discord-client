package cz.salmelu.discord.resources;

import java.util.List;

public interface Member {
    String getId();
    User getUser();
    String getNickname();
    List<Role> getRoles();
    String getMention();
}
