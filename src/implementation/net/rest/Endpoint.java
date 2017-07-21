package cz.salmelu.discord.implementation.net.rest;

import java.util.*;

public class Endpoint {
    private static final String ADDR_BASE = "https://discordapp.com/api";
    private static final String ADDR_GATEWAY = ADDR_BASE + "/gateway";
    private static final String ADDR_CHANNEL = ADDR_BASE + "/channels";
    private static final String ADDR_SERVER = ADDR_BASE + "/guilds";
    private static final String ADDR_USER = ADDR_BASE + "/users";
    private static final String ADDR_MY_USER = ADDR_USER + "/@me";

    public static final Endpoint BASE = new Endpoint(Arrays.asList(ADDR_BASE));
    public static final Endpoint GATEWAY = new Endpoint(Arrays.asList(ADDR_GATEWAY));
    public static final Endpoint CHANNEL = new Endpoint(Arrays.asList(ADDR_CHANNEL));
    public static final Endpoint SERVER = new Endpoint(Arrays.asList(ADDR_SERVER));
    public static final Endpoint USER = new Endpoint(Arrays.asList(ADDR_USER));
    public static final Endpoint MY_USER = new Endpoint(Arrays.asList(ADDR_MY_USER));

    private final String base;
    private final String address;
    private final String suffix;
    private final String tail;
    private List<String> elements;
    private Map<String, String> params;

    public Endpoint(List<String> elements) {
        this(elements, null);
    }

    public Endpoint(List<String> elements, Map<String, String> params) {
        this.elements = elements;
        this.base = elements.get(0);
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < elements.size(); ++i) {
            if(i > 1) sb.append('/');
            sb.append(elements.get(i));
        }
        this.suffix = sb.toString();

        this.params = params == null ? new HashMap<>() : params;
        StringBuilder pb = new StringBuilder();
        for(Map.Entry<String, String> param : params.entrySet()) {
            if(pb.length() > 0) pb.append('&');
            pb.append(param.getKey()).append('=').append(param.getValue());
        }
        tail = pb.toString();

        StringBuilder ab = new StringBuilder(100);
        ab.append(this.base);
        if(elements.size() > 1) ab.append('/').append(this.suffix);
        if(params.size() > 0) ab.append('?').append(tail);
        this.address = ab.toString();
    }

    public String getAddress() {
        return address;
    }

    public String getElement(int id) {
        return elements.get(id);
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public String getSuffix() {
        return suffix;
    }

    public String getBase() {
        return base;
    }

    public String getTail() {
        return tail;
    }

    public boolean isServer() {
        return address.startsWith(ADDR_SERVER);
    }

    public boolean isChannel() {
        return address.startsWith(ADDR_CHANNEL);
    }
}
