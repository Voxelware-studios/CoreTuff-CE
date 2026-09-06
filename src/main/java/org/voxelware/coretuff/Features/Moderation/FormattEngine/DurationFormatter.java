package org.voxelware.coretuff.Features.Moderation.FormattEngine;

public final class DurationFormatter {

	private DurationFormatter() {}

	public static String format(long millis) {
		if (millis <= 0L) {
			return "0s";
		}

		long seconds = millis / 1000L;
		long minutes = seconds / 60L;
		long hours = minutes / 60L;
		long days = hours / 24L;

		seconds %= 60L;
		minutes %= 60L;
		hours %= 24L;

		StringBuilder builder = new StringBuilder();
		if (days > 0L) {
			builder.append(days).append("d ");
		}
		if (hours > 0L) {
			builder.append(hours).append("h ");
		}
		if (minutes > 0L) {
			builder.append(minutes).append("m ");
		}
		if (seconds > 0L) {
			builder.append(seconds).append("s");
		}

		return builder.toString().trim();
	}
}
