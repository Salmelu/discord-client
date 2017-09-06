package cz.salmelu.discord.implementation.net.rest;

import java.util.*;

/**
 * Represents a Discord endpoint for a specific resource.
 */
public class Endpoint {
    private static final String ADDR_BASE = "https://discordapp.com/api";
    private static final String ADDR_GATEWAY = ADDR_BASE + "/gateway";
    private static final String ADDR_CHANNEL = ADDR_BASE + "/channels";
    private static final String ADDR_SERVER = ADDR_BASE + "/guilds";
    private static final String ADDR_USER = ADDR_BASE + "/users";
    private static final String ADDR_MY_USER = ADDR_USER + "/@me";

    public static final Endpoint BASE = new Endpoint(Collections.singletonList(ADDR_BASE));
    public static final Endpoint GATEWAY = new Endpoint(Collections.singletonList(ADDR_GATEWAY));
    public static final Endpoint CHANNEL = new Endpoint(Collections.singletonList(ADDR_CHANNEL));
    public static final Endpoint SERVER = new Endpoint(Collections.singletonList(ADDR_SERVER));
    public static final Endpoint USER = new Endpoint(Collections.singletonList(ADDR_USER));
    public static final Endpoint MY_USER = new Endpoint(Collections.singletonList(ADDR_MY_USER));

    private final String base;
    private final String address;
    private final String suffix;
    private final String tail;
    private List<String> elements;
    private Map<String, String> params;

    /**
     * Constructs a new endpoint instance from given elements.
     * @param elements parts of the endpoint address
     */
    Endpoint(List<String> elements) {
        this(elements, null);
    }

    /**
     * Constructs a new endpoint instance from given elements and parameters.
     * @param elements parts of the endpoint address
     * @param params optional URL parameters
     */
    Endpoint(List<String> elements, Map<String, String> params) {
        this.elements = elements;
        this.base = elements.get(0);
        final StringBuilder sb = new StringBuilder();
        for(int i = 1; i < elements.size(); ++i) {
            if(i > 1) sb.append('/');
            sb.append(elements.get(i));
        }
        this.suffix = sb.toString();

        this.params = params == null ? new HashMap<>() : params;
        if(params != null) {
            final StringBuilder pb = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (pb.length() > 0) pb.append('&');
                pb.append(param.getKey()).append('=').append(param.getValue());
            }
            tail = pb.toString();
        }
        else {
            tail = "";
        }

        final StringBuilder ab = new StringBuilder(100);
        ab.append(this.base);
        if(elements.size() > 1) ab.append('/').append(this.suffix);
        if(params != null && params.size() > 0) ab.append('?').append(tail);
        this.address = ab.toString();
    }

    /**
     * Gets full endpoint address.
     * @return full endpoint address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets a specific element in the endpoint chain.
     * @param pos position of the element
     * @return element at given position, or null
     */
    public String getElement(int pos) {
        return elements.get(pos);
    }

    /**
     * Gets a parameter with a given key
     * @param key param key
     * @return param value if it exists, or null
     */
    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * Gets full suffix of the address, those are all the elements except the first one (the base).
     * @return address suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Gets the base of the address. This defines the rate limits.
     * @return address base
     */
    public String getBase() {
        return base;
    }

    /**
     * Gets the tail of the address. The tail are concatenated parameters, joined with &.
     * @return address tail
     */
    public String getTail() {
        return tail;
    }

    /**
     * Checks if the endpoint is pointing to a server.
     * @return true if the endpoint should be considered a server endpoint
     */
    public boolean isServer() {
        return address.startsWith(ADDR_SERVER);
    }

    /**
     * Checks if the endpoint is pointing to a channel.
     * @return true if the endpoint should be considered a channel endpoint
     */
    public boolean isChannel() {
        return address.startsWith(ADDR_CHANNEL);
    }
}
