package com.tanay.chesslab.worker.stockfish;

public class StockfishUnavailableException extends RuntimeException {

	public StockfishUnavailableException(String message) {
		super(message);
	}

	public StockfishUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
