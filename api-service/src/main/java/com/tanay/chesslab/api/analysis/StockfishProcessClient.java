package com.tanay.chesslab.api.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockfishProcessClient {

	private final String executable;

	public StockfishProcessClient(@Value("${stockfish.path:stockfish}") String executable) {
		this.executable = executable;
	}

	public EngineEvaluation analyzeFen(String fen, int depth) {
		int requestedDepth = Math.max(1, depth);
		Process process = startProcess();
		try (
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {

			send(writer, "uci");
			readUntil(reader, "uciok");
			send(writer, "isready");
			readUntil(reader, "readyok");
			send(writer, "ucinewgame");
			send(writer, "position fen " + fen);
			send(writer, "go depth " + requestedDepth);

			StockfishOutputParser parser = new StockfishOutputParser();
			String line;
			while ((line = reader.readLine()) != null) {
				parser.accept(line);
				if (line.startsWith("bestmove ")) {
					return parser.toEvaluation(requestedDepth);
				}
			}
			throw new StockfishUnavailableException("Stockfish exited before returning a best move");
		} catch (IOException error) {
			throw new StockfishUnavailableException("Could not communicate with Stockfish at " + executable, error);
		} finally {
			process.destroyForcibly();
		}
	}

	private Process startProcess() {
		try {
			return new ProcessBuilder(executable).redirectErrorStream(true).start();
		} catch (IOException error) {
			throw new StockfishUnavailableException("Could not start Stockfish at " + executable, error);
		}
	}

	private static void send(BufferedWriter writer, String command) throws IOException {
		writer.write(command);
		writer.newLine();
		writer.flush();
	}

	private static void readUntil(BufferedReader reader, String expectedLine) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (expectedLine.equals(line)) {
				return;
			}
		}
		throw new StockfishUnavailableException("Stockfish exited before " + expectedLine);
	}
}
