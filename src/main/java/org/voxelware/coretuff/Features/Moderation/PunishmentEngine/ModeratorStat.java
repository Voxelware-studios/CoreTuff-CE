package org.voxelware.coretuff.Features.Moderation.PunishmentEngine;

public class ModeratorStat {

    private final String moderator;
    private final int count;

    public ModeratorStat(String moderator, int count) {
        this.moderator = moderator;
        this.count = count;
    }

    public String getModerator() { return moderator; }
    public int getCount() { return count; }
}
