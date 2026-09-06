package org.voxelware.coretuff.Features.Moderation.FormattEngine;


import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class BanFormatter {

	private static final SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);

	private BanFormatter() {}

	public static String formatDisconnectMessage() {
		return ModerationMessages.get(
				"moderation.ban-screen.disconnect",
				"You are banned."
		);
	}

	public static String format(String reason, long expires, String moderator, String id) {
		String disconnect = ModerationMessages.get("moderation.ban-screen.disconnect", "You are banned.");
		String reasonLabel = ModerationMessages.get("moderation.ban-screen.reason-label", "Reason");
		String moderatorLabel = ModerationMessages.get("moderation.ban-screen.moderator-label", "Moderator");
		String banIdLabel = ModerationMessages.get("moderation.ban-screen.ban-id-label", "Ban ID");
		String expiresLabel = ModerationMessages.get("moderation.ban-screen.expires-label", "Expires");
		String remainingLabel = ModerationMessages.get("moderation.ban-screen.remaining-label", "Remaining");
		String durationLabel = ModerationMessages.get("moderation.ban-screen.duration-label", "Duration");
		String permanentText = ModerationMessages.get("moderation.ban-screen.permanent-text", "Permanent");
		String appealLabel = ModerationMessages.get("moderation.ban-screen.appeal-label", "Appeal");
		String appealText = ModerationMessages.get("moderation.ban-screen.appeal-text", "Contact server staff");

		StringBuilder builder = new StringBuilder();
		builder.append(disconnect).append("\n\n");
		builder.append(reasonLabel).append(": ").append(reason).append('\n');
		builder.append(moderatorLabel).append(": ").append(moderator == null ? "Unknown" : moderator).append('\n');
		builder.append(banIdLabel).append(": #").append(id).append('\n');

		if (expires == -1L) {
			builder.append(durationLabel).append(": ").append(permanentText).append('\n');
		} else {
			builder.append(expiresLabel).append(": ").append(DATE_FORMAT.format(new Date(expires))).append('\n');
			builder.append(remainingLabel).append(": ")
					.append(DurationFormatter.format(expires - System.currentTimeMillis()))
					.append('\n');
		}

		builder.append("\n").append(appealLabel).append(": ").append(appealText);
		return builder.toString();
	}
}
