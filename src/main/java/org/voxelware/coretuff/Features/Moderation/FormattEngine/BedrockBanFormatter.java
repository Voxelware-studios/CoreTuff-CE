package org.voxelware.coretuff.Features.Moderation.FormattEngine;


import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;

public final class BedrockBanFormatter {
	
	
	private BedrockBanFormatter() {}
	
	public static String format(String reason, String id) {
		String disconnect = ModerationMessages.get(
				"moderation.bedrock-ban-screen.disconnect",
				"You are banned."
		);
		String reasonLabel = ModerationMessages.get(
				"moderation.bedrock-ban-screen.reason-label",
				"Reason"
		);
		String idLabel = ModerationMessages.get(
				"moderation.bedrock-ban-screen.id-label",
				"ID"
		);
		
		return """
                %s
                
                %s: %s
                %s: #%s
                """.formatted(
				disconnect,
				reasonLabel,
				reason,
				idLabel,
				id
		);
	}
}
