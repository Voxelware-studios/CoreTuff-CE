package org.voxelware.coretuff.Features.Moderation.ModIO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {

	private static final Pattern PATTERN = Pattern.compile("(\\d+)(mo|[smhdwy])");

	private DurationParser() {}

	public static long parse(String input) {
		if (input == null || input.isBlank()) {
			throw new IllegalArgumentException("Duration cannot be empty.");
		}

		Matcher matcher = PATTERN.matcher(input.toLowerCase());
		long total = 0L;
		int matchedLength = 0;

		while (matcher.find()) {
			long value = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);
			matchedLength += matcher.group(0).length();

			total += switch (unit) {
				case "s" -> value * 1_000L;
				case "m" -> value * 60_000L;
				case "h" -> value * 3_600_000L;
				case "d" -> value * 86_400_000L;
				case "w" -> value * 604_800_000L;
				case "mo" -> value * 2_592_000_000L;
				case "y" -> value * 31_536_000_000L;
				default -> throw new IllegalArgumentException("Unsupported duration unit: " + unit);
			};
		}

		if (total <= 0L || matchedLength != input.length()) {
			throw new IllegalArgumentException("Invalid duration format: " + input);
		}

		return total;
	}
}
