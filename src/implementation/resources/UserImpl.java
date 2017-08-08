package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.Channel;
import cz.salmelu.discord.resources.PrivateChannel;
import org.json.JSONObject;

public class UserImpl implements cz.salmelu.discord.resources.User {

    private final UserObject originalObject;
    private final ClientImpl client;

    public UserImpl(ClientImpl client, UserObject userObject) {
        this.originalObject = userObject;
        this.client = client;
    }

    public void update(UserObject updatedObject) {
        this.originalObject.setAvatarHash(updatedObject.getAvatarHash());
        this.originalObject.setBot(updatedObject.isBot());
        this.originalObject.setDiscriminator(updatedObject.getDiscriminator());
        this.originalObject.setEmail(updatedObject.getEmail());
        this.originalObject.setMfaEnabled(updatedObject.isMfaEnabled());
        this.originalObject.setUsername(updatedObject.getUsername());
        this.originalObject.setVerified(updatedObject.isVerified());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof UserImpl))return false;
        UserImpl otherCast = (UserImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String getId() {
        return originalObject.getId();
    }

    @Override
    public String getName() {
        return originalObject.getUsername();
    }

    @Override
    public String getDiscriminator() {
        return originalObject.getDiscriminator();
    }

    @Override
    public String getMention() {
        return "<@" + getId() + ">";
    }

    @Override
    public PrivateChannel createPrivateChannel() {
        final Endpoint endpoint = EndpointBuilder.create(Endpoint.USER).addElement("@me").addElement("channels").build();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("recipient_id", getId());
        JSONObject replyObject = client.getRequester().postRequestAsObject(endpoint, jsonObject);
        if(replyObject == null) return null;
        PrivateChannelObject channelObject = client.getSerializer().deserialize(replyObject,
                PrivateChannelObject.class);
        Channel channel = client.getChannelById(channelObject.getId());
        if(channel == null) {
            PrivateChannelImpl newChannel = new PrivateChannelImpl(client, channelObject, null);
            client.addChannel(newChannel);
            return newChannel;
        }
        return channel.toPrivateChannel();
    }
}
