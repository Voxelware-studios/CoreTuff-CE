package org.voxelware.coretuff.Features.Moderation.ModIO;

public class BanCheckResult {
	
	private final boolean banned;
	private final String message;
	
	public BanCheckResult(boolean banned, String message) {
		this.banned = banned;
		this.message = message;
	}
	
	public boolean isBanned() {
		return banned;
	}
	
	public String getMessage() {
		return message;
	}
}