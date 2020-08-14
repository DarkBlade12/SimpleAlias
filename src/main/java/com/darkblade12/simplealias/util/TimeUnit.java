package com.darkblade12.simplealias.util;

public enum TimeUnit {
	WEEK {
		@Override
		public long convert(long time) {
			return time / (7 * 24 * 60 * 60 * 1000);
		}
	},
	DAY {
		@Override
		public long convert(long time) {
			return time / (24 * 60 * 60 * 1000) % 7;
		}
	},
	HOUR {
		@Override
		public long convert(long time) {
			return time / (60 * 60 * 1000) % 24;
		}
	},
	MINUTE {
		@Override
		public long convert(long time) {
			return time / (60 * 1000) % 60;
		}
	},
	SECOND {
		@Override
		public long convert(long time) {
			return time / 1000 % 60;
		}
	};

	public abstract long convert(long time);

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public static String convertToString(long time) {
		if (time < 0)
			throw new IllegalArgumentException("Time value cannot be lower than 0");
		StringBuilder s = new StringBuilder();
		for (TimeUnit unit : TimeUnit.values()) {
			long amount = unit.convert(time);
			if (amount > 0) {
				if (s.length() > 0)
					s.append(", ");
				s.append(amount + " " + unit + (amount == 1 ? "" : "s"));
			}
		}
		int index = s.lastIndexOf(", ");
		if (index != -1)
			s.replace(index, index + 2, " and ");
		return s.length() > 0 ? s.toString() : "a few milliseconds";
	}
}