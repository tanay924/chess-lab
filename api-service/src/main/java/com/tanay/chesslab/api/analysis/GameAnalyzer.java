package com.tanay.chesslab.api.analysis;

import java.util.List;

import com.tanay.chesslab.api.domain.GameDetail;
import com.tanay.chesslab.api.domain.MoveEvaluation;

@FunctionalInterface
public interface GameAnalyzer {

	List<MoveEvaluation> analyze(GameDetail game);
}
