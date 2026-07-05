package com.tanay.chesslab.api.analysis;

import java.util.Optional;

public class StockfishOutputParser {

	private String bestMove;
	private int scoreCp;

	public void accept(String line) {
		if (line == null || line.isBlank()) {
			return;
		}
		if (line.startsWith("info ")) {
			parseScore(line).ifPresent(score -> scoreCp = score);
			return;
		}
		if (line.startsWith("bestmove ")) {
			String[] parts = line.split("\\s+");
			if (parts.length >= 2) {
				bestMove = parts[1];
			}
		}
	}

	public EngineEvaluation toEvaluation(int depth) {
		if (bestMove == null || bestMove.isBlank()) {
			throw new StockfishUnavailableException("Stockfish did not return a best move");
		}
		return new EngineEvaluation(bestMove, scoreCp, depth);
	}

	private static Optional<Integer> parseScore(String line) {
		String[] parts = line.split("\\s+");
		for (int index = 0; index < parts.length - 2; index++) {
			if (!"score".equals(parts[index])) {
				continue;
			}
			if ("cp".equals(parts[index + 1])) {
				return parseInteger(parts[index + 2]);
			}
			if ("mate".equals(parts[index + 1])) {
				return parseInteger(parts[index + 2]).map(StockfishOutputParser::mateToCentipawns);
			}
		}
		return Optional.empty();
	}

	private static Optional<Integer> parseInteger(String value) {
		try {
			return Optional.of(Integer.parseInt(value));
		} catch (NumberFormatException error) {
			return Optional.empty();
		}
	}

	private static int mateToCentipawns(int mateDistance) {
		int sign = mateDistance < 0 ? -1 : 1;
		return sign * 100_000;
	}
}
