package cz.salmelu.discord.listeners;

import cz.salmelu.discord.resources.Member;

public interface ServerListener {

    default void onMemberAdd(Member member) {

    }
}
