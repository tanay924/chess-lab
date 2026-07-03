package com.tanay.chesslab.api.web;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanay.chesslab.api.domain.AnalysisJob;
import com.tanay.chesslab.api.domain.AnalysisReport;
import com.tanay.chesslab.api.domain.CreateGameRequest;
import com.tanay.chesslab.api.domain.GameDetail;
import com.tanay.chesslab.api.domain.GameSummary;
import com.tanay.chesslab.api.service.GameService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/games")
public class GameController {

	private final GameService games;

	public GameController(GameService games) {
		this.games = games;
	}

	@PostMapping
	public GameDetail createGame(@Valid @RequestBody CreateGameRequest request) {
		return games.createGame(request);
	}

	@GetMapping
	public List<GameSummary> listGames() {
		return games.listGames();
	}

	@GetMapping("/{gameId}")
	public GameDetail getGame(@PathVariable String gameId) {
		return games.getGame(gameId);
	}

	@PostMapping("/{gameId}/analysis")
	public AnalysisJob startAnalysis(@PathVariable String gameId) {
		return games.startAnalysis(gameId);
	}

	@GetMapping("/{gameId}/analysis-report")
	public AnalysisReport getReport(@PathVariable String gameId) {
		return games.getReport(gameId);
	}
}
