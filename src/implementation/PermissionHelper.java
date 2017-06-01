package cz.salmelu.discord.implementation;


public class PermissionHelper {
    public static boolean canCreateInstantInvite(long permissions) {
        return (permissions & 0x00000001) > 0;
    }

    public static boolean canKickMembers(long permissions) {
        return (permissions & 0x00000002) > 0;
    }

    public static boolean canBanMembers(long permissions) {
        return (permissions & 0x00000004) > 0;
    }

    public static boolean isAdministrator(long permissions) {
        return (permissions & 0x00000008) > 0;
    }

    public static boolean canManageChannels(long permissions) {
        return (permissions & 0x00000010) > 0;
    }

    public static boolean canManageGuild(long permissions) {
        return (permissions & 0x00000020) > 0;
    }

    public static boolean canAddReactions(long permissions) {
        return (permissions & 0x00000040) > 0;
    }

    public static boolean canReadMessages(long permissions) {
        return (permissions & 0x00000400) > 0;
    }

    public static boolean canSendMessages(long permissions) {
        return (permissions & 0x00000800) > 0;
    }

    public static boolean canSendTTSMessages(long permissions) {
        return (permissions & 0x00001000) > 0;
    }

    public static boolean canManageMessages(long permissions) {
        return (permissions & 0x00002000) > 0;
    }

    public static boolean canEmbedLinks(long permissions) {
        return (permissions & 0x00004000) > 0;
    }

    public static boolean canAttachFiles(long permissions) {
        return (permissions & 0x00008000) > 0;
    }

    public static boolean canReadMessageHistory(long permissions) {
        return (permissions & 0x00010000) > 0;
    }

    public static boolean canMentionEveryone(long permissions) {
        return (permissions & 0x00020000) > 0;
    }

    public static boolean canUseExternalEmojis(long permissions) {
        return (permissions & 0x00040000) > 0;
    }

    public static boolean canVoiceConnect(long permissions) {
        return (permissions & 0x00100000) > 0;
    }

    public static boolean canVoiceSpeak(long permissions) {
        return (permissions & 0x00200000) > 0;
    }

    public static boolean canVoiceMute(long permissions) {
        return (permissions & 0x00400000) > 0;
    }

    public static boolean canVoiceDeafen(long permissions) {
        return (permissions & 0x00800000) > 0;
    }

    public static boolean canVoiceMove(long permissions) {
        return (permissions & 0x01000000) > 0;
    }

    public static boolean canUseVoiceActivityDetection(long permissions) {
        return (permissions & 0x02000000) > 0;
    }

    public static boolean canChangeNickname(long permissions) {
        return (permissions & 0x04000000) > 0;
    }

    public static boolean canManageNicknames(long permissions) {
        return (permissions & 0x08000000) > 0;
    }

    public static boolean canManageRoles(long permissions) {
        return (permissions & 0x10000000) > 0;
    }

    public static boolean canManageWebhooks(long permissions) {
        return (permissions & 0x20000000) > 0;
    }

    public static boolean canManageEmojis(long permissions) {
        return (permissions & 0x40000000) > 0;
    }
}