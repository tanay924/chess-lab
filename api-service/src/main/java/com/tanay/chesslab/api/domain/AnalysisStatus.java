package com.tanay.chesslab.api.domain;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AnalysisStatus {
	QUEUED,
	RUNNING,
	READY,
	FAILED;

	@JsonValue
	public String jsonValue() {
		return name().toLowerCase(Locale.ROOT);
	}
}
