package com.tanay.chesslab.api.analysis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tanay.chesslab.api.domain.GameDetail;
import com.tanay.chesslab.api.domain.MoveEvaluation;
import com.tanay.chesslab.api.domain.MoveRecord;

@Service
public class StockfishGameAnalyzer implements GameAnalyzer {

	private final StockfishProcessClient stockfish;
	private final int depth;
	private final int maxPlies;

	public StockfishGameAnalyzer(
			StockfishProcessClient stockfish,
			@Value("${analysis.stockfish.depth:8}") int depth,
			@Value("${analysis.max-plies:80}") int maxPlies) {
		this.stockfish = stockfish;
		this.depth = depth;
		this.maxPlies = maxPlies;
	}

	@Override
	public List<MoveEvaluation> analyze(GameDetail game) {
		List<MoveEvaluation> evaluations = new ArrayList<>();
		int ply = 1;
		for (MoveRecord move : game.moves()) {
			ply = analyzeIfPresent(evaluations, ply, move.whiteMove(), move.whiteUci(), move.whiteFenBefore());
			ply = analyzeIfPresent(evaluations, ply, move.blackMove(), move.blackUci(), move.blackFenBefore());
			if (evaluations.size() >= maxPlies) {
				break;
			}
		}
		if (evaluations.isEmpty()) {
			throw new StockfishUnavailableException("This game needs to be re-imported before analysis; move FENs are missing.");
		}
		return evaluations;
	}

	private int analyzeIfPresent(List<MoveEvaluation> evaluations, int ply, String san, String uci, String fenBefore) {
		if (san == null || san.isBlank()) {
			return ply;
		}
		if (fenBefore == null || fenBefore.isBlank()) {
			throw new StockfishUnavailableException("This game needs to be re-imported before analysis; move FENs are missing.");
		}
		EngineEvaluation engine = stockfish.analyzeFen(fenBefore, depth);
		evaluations.add(new MoveEvaluation(
				ply,
				san,
				engine.bestMove(),
				engine.scoreCp(),
				classify(uci, engine.bestMove())));
		return ply + 1;
	}

	private static String classify(String playedUci, String bestUci) {
		if (playedUci != null && playedUci.equalsIgnoreCase(bestUci)) {
			return "excellent";
		}
		return "inaccuracy";
	}
}
