package org.voxelware.coretuff.Features.Moderation.PunishmentEngine;

public class PunishmentRecord {
	
	private final String id;
	
	private final String uuid;
	private final String name;
	private final String ip;
	
	private final String type;
	private final String reason;
	private final String moderator;
	
	private final long createdAt;
	private final long expiresAt;
	
	private final boolean active;
	private final long removedAt;
	
	private final String pardonReason;
	private final String pardonedBy;
	
	public PunishmentRecord(
			String id,
			String uuid,
			String name,
			String type,
			String reason,
			String moderator,
			long createdAt,
			long expiresAt,
			String ip,
			boolean active,
			long removedAt,
			String pardonReason,
			String pardonedBy
	) {
		
		this.id = id;
		
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		
		this.type = type;
		this.reason = reason;
		this.moderator = moderator;
		
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		
		this.active = active;
		this.removedAt = removedAt;
		
		this.pardonReason = pardonReason;
		this.pardonedBy = pardonedBy;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIp() {
		return ip;
	}
	
	public String getType() {
		return type;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getModerator() {
		return moderator;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
	
	public long getExpiresAt() {
		return expiresAt;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public long getRemovedAt() {
		return removedAt;
	}
	
	public String getPardonReason() {
		return pardonReason;
	}
	
	public String getPardonedBy() {
		return pardonedBy;
	}
	
	public boolean isPermanent() {
		return expiresAt == -1L;
	}
	
	public boolean isExpired() {
		
		return expiresAt != -1L
				&& System.currentTimeMillis() > expiresAt;
	}
}