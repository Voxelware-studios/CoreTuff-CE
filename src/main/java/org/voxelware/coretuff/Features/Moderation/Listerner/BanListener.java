package org.voxelware.coretuff.Features.Moderation.Listerner;


import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;

public class BanListener extends PreLoginListener {

	public BanListener(ModerationService moderationService) {
		super(moderationService);
	}
}
