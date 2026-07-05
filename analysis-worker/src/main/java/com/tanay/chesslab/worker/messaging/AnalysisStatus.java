package com.tanay.chesslab.worker.messaging;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AnalysisStatus {
	RUNNING,
	READY,
	FAILED;

	@JsonValue
	public String jsonValue() {
		return name().toLowerCase(Locale.ROOT);
	}
}
