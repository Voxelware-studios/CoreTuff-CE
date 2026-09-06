package org.voxelware.coretuff.Features.Moderation.PunishmentEngine;

public class ModerationSummary {

    private final int totalPunishments;
    private final int activePunishments;
    private final int bans;
    private final int tempBans;
    private final int ipBans;
    private final int pardoned;
    private final int expired;
    private final int thisWeek;
    private final int thisMonth;

    public ModerationSummary(int totalPunishments, int activePunishments, int bans,
                             int tempBans, int ipBans, int pardoned, int expired,
                             int thisWeek, int thisMonth) {
        this.totalPunishments = totalPunishments;
        this.activePunishments = activePunishments;
        this.bans = bans;
        this.tempBans = tempBans;
        this.ipBans = ipBans;
        this.pardoned = pardoned;
        this.expired = expired;
        this.thisWeek = thisWeek;
        this.thisMonth = thisMonth;
    }

    public int getTotalPunishments() { return totalPunishments; }
    public int getActivePunishments() { return activePunishments; }
    public int getBans() { return bans; }
    public int getTempBans() { return tempBans; }
    public int getIpBans() { return ipBans; }
    public int getPardoned() { return pardoned; }
    public int getExpired() { return expired; }
    public int getThisWeek() { return thisWeek; }
    public int getThisMonth() { return thisMonth; }
}
