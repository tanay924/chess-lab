package com.tanay.chesslab.api.analysis;

public class StockfishUnavailableException extends RuntimeException {

	public StockfishUnavailableException(String message) {
		super(message);
	}

	public StockfishUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
