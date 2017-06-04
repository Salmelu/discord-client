package cz.salmelu.discord.implementation.net;

public class Endpoint {
    public static final String BASE = "https://discordapp.com/api";
    public static final String GATEWAY = BASE + "/gateway";
    public static final String CHANNEL = BASE + "/channels";
    public static final String SERVER = BASE + "/guilds";
    public static final String USER = BASE + "/users";

    public static final String MY_USER = USER + "/@me";
}
