package com.tanay.chesslab.worker.stockfish;

public interface StockfishClient {

	EngineEvaluation analyzeFen(String fen, int depth);
}
