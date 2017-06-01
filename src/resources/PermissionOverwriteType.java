package cz.salmelu.discord.resources;

import com.google.gson.annotations.SerializedName;

public enum PermissionOverwriteType {
    @SerializedName("role")
    ROLE,
    @SerializedName("member")
    MEMBER
}
