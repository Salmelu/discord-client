package cz.salmelu.discord.implementation.net.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EndpointBuilder {

    private ArrayList<String> elements;
    private HashMap<String, String> params;

    public static EndpointBuilder create(Endpoint base) {
        return new EndpointBuilder(base);
    }

    private EndpointBuilder(Endpoint base) {
        elements = new ArrayList<>();
        params = new HashMap<>();
        elements.add(base.getBase());
    }

    public EndpointBuilder addElement(String element) {
        elements.add(element);
        return this;
    }

    public EndpointBuilder addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public EndpointBuilder addParamMap(Map<String, String> params) {
        params.putAll(params);
        return this;
    }

    public Endpoint build() {
        return new Endpoint(elements, params);
    }
}
