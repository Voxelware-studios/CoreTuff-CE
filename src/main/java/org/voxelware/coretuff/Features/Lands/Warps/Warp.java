package org.voxelware.coretuff.Features.Lands.Warps;

public class Warp {

    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final String ownerUuid;
    private final String description;
    private final long createdAt;

    public Warp(String name, String world, double x, double y, double z, float yaw, float pitch,
                String ownerUuid, String description, long createdAt) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ownerUuid = ownerUuid;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getName() { return name; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public String getOwnerUuid() { return ownerUuid; }
    public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }
}
