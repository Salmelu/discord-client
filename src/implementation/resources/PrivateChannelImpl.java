package cz.salmelu.discord.implementation.resources;

import cz.salmelu.discord.implementation.json.resources.MessageObject;
import cz.salmelu.discord.implementation.json.resources.PrivateChannelObject;
import cz.salmelu.discord.implementation.json.resources.UserObject;
import cz.salmelu.discord.implementation.net.rest.Endpoint;
import cz.salmelu.discord.implementation.net.rest.EndpointBuilder;
import cz.salmelu.discord.resources.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivateChannelImpl extends ChannelBase implements PrivateChannel {

    private final String id;
    private final PrivateChannelObject originalObject;
    private final List<User> users;

    public PrivateChannelImpl(ClientImpl client, PrivateChannelObject channelObject, List<User> users) {
        this.id = channelObject.getId();
        this.originalObject = channelObject;
        this.client = client;
        if(users == null) {
            users = new ArrayList<>();
            for (UserObject userObject : originalObject.getRecipients()) {
                UserImpl user = client.getUser(userObject.getId());
                if(user == null) {
                    user = new UserImpl(client, userObject);
                    client.addUser(user);
                }
                users.add(user);
            }
            this.users = users;
        }
        else {
            this.users = users;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof PrivateChannelImpl)) return false;
        PrivateChannelImpl otherCast = (PrivateChannelImpl) other;
        return otherCast.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode() * 71;
    }

    @Override
    public void messageArrived(Message message) {
        originalObject.setLastMessageId(message.getId());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public boolean canSendMessage() {
        return true;
    }

    @Override
    public void sendMessage(String text) {
        MessageObject messageObject = new MessageObject();
        messageObject.setContent(text);
        client.getRequester().postRequest(EndpointBuilder.create(Endpoint.CHANNEL)
                .addElement(id).addElement("messages").build(), messageObject);
    }

    @Override
    public ServerChannel toServerChannel() {
        return null;
    }

    @Override
    public PrivateChannel toPrivateChannel() {
        return this;
    }

    @Override
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
