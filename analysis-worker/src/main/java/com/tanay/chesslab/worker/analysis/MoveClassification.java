package com.tanay.chesslab.worker.analysis;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MoveClassification {
	EXCELLENT,
	GOOD,
	INACCURACY,
	MISTAKE,
	BLUNDER;

	@JsonValue
	public String jsonValue() {
		return name().toLowerCase(Locale.ROOT);
	}
}
