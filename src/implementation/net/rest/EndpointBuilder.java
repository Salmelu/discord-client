package cz.salmelu.discord.implementation.net.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder class for endpoints.
 */
public class EndpointBuilder {

    private ArrayList<String> elements;
    private HashMap<String, String> params;

    /**
     * Create a new builder instance for a given base endpoint.
     * @param base endpoint base
     * @return an instance of builder
     */
    public static EndpointBuilder create(Endpoint base) {
        return new EndpointBuilder(base);
    }

    private EndpointBuilder(Endpoint base) {
        elements = new ArrayList<>();
        params = new HashMap<>();
        elements.add(base.getBase());
    }

    /**
     * Adds a new element to the endpoint address.
     * @param element new element
     * @return instance of this builder
     */
    public EndpointBuilder addElement(String element) {
        elements.add(element);
        return this;
    }

    /**
     * Adds a new parameter to the endpoint address.
     * @param key param key
     * @param value param value
     * @return instance of this builder
     */
    public EndpointBuilder addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    /**
     * Adds multiple parameters at once, stored in the map.
     * @param params map of parameters
     * @return instance of this builder
     */
    public EndpointBuilder addParamMap(Map<String, String> params) {
        this.params.putAll(params);
        return this;
    }

    /**
     * Builds an endpoint from previously added parts and returns a reference to it.
     * @return a new endpoint instance
     */
    public Endpoint build() {
        return new Endpoint(elements, params);
    }
}
