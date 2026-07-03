package com.tanay.chesslab.api.domain;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreateGameRequest(
		@NotBlank String pgn,
		String white,
		String black,
		String result,
		Integer plyCount,
		String finalFen,
		List<MoveRecord> moves) {
}
