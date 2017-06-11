package cz.salmelu.discord;

import cz.salmelu.discord.resources.Member;
import cz.salmelu.discord.resources.Server;

public class NameMatcher {
    public static Member matchMember(Server server, String nameString) {
        nameString = nameString.trim();
        if(nameString.matches("<@[0-9]+>")) {
            // It's a user mention
            String id = nameString.substring(2, nameString.length() - 1);
            return server.getMemberById(id);
        }
        else if(nameString.matches("<@![0-9]+>")) {
            // It's a member mention
            String id = nameString.substring(3, nameString.length() - 1);
            return server.getMemberById(id);
        }
        else {
            return server.getMemberByNickname(nameString);
        }
    }
}
