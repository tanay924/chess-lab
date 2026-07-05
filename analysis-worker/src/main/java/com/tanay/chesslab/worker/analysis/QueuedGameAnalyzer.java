package com.tanay.chesslab.worker.analysis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tanay.chesslab.worker.messaging.AnalysisRequest;
import com.tanay.chesslab.worker.messaging.MoveRecord;
import com.tanay.chesslab.worker.stockfish.EngineEvaluation;
import com.tanay.chesslab.worker.stockfish.StockfishClient;
import com.tanay.chesslab.worker.stockfish.StockfishUnavailableException;

@Service
public class QueuedGameAnalyzer implements GameAnalysisRunner {

	private final StockfishClient stockfish;

	public QueuedGameAnalyzer(StockfishClient stockfish) {
		this.stockfish = stockfish;
	}

	@Override
	public List<MoveEvaluation> analyze(AnalysisRequest request) {
		List<MoveEvaluation> evaluations = new ArrayList<>();
		int ply = 1;
		for (MoveRecord move : safeMoves(request.moves())) {
			ply = analyzeIfPresent(evaluations, ply, move.whiteMove(), move.whiteUci(), move.whiteFenBefore(),
					request.depth());
			if (evaluations.size() >= request.maxPlies()) {
				break;
			}
			ply = analyzeIfPresent(evaluations, ply, move.blackMove(), move.blackUci(), move.blackFenBefore(),
					request.depth());
			if (evaluations.size() >= request.maxPlies()) {
				break;
			}
		}
		if (evaluations.isEmpty()) {
			throw new StockfishUnavailableException("This game needs to be re-imported before analysis; move FENs are missing.");
		}
		return evaluations;
	}

	private int analyzeIfPresent(
			List<MoveEvaluation> evaluations,
			int ply,
			String san,
			String uci,
			String fenBefore,
			int depth) {
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

	private static List<MoveRecord> safeMoves(List<MoveRecord> moves) {
		return moves == null ? List.of() : moves;
	}

	private static String classify(String playedUci, String bestUci) {
		if (playedUci != null && playedUci.equalsIgnoreCase(bestUci)) {
			return "excellent";
		}
		return "inaccuracy";
	}
}
