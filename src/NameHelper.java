package cz.salmelu.discord;

import cz.salmelu.discord.resources.Member;
import cz.salmelu.discord.resources.Server;

/**
 * <p>A simple class to work with name checks and conversions.</p>
 */
public class NameHelper {
    /**
     * <p>Tries to match the member to any of the server members.</p>
     * @param server the server where the mentions were used
     * @param nameString the string possibly representing a name
     * @return a member reference if successful, otherwise null
     */
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

    /**
     * <p>Checks if the given name is a valid user name or nickname according to discord rules.</p>
     * <p>Discord names must be between 2 and 32 characters long,
     * must not contain some special characters (@, #, :, ```)
     * and must not be equal to keywords (discordtag, here, everyone).</p>
     * @param name checked name
     * @return true if given name can be set as a nickname or username
     */
    public static boolean validateName(String name) {
        if(name.length() < 2 || name.length() > 32) return false;
        if(name.contains("@") || name.contains("#") || name.contains(":") || name.contains("```")) return false;
        if(name.equals("discordtag") || name.equals("everyone") || name.equals("here")) return false;
        return true;
    }
}
