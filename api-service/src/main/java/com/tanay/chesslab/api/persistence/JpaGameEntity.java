package com.tanay.chesslab.api.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
class JpaGameEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String whiteName;

	@Column(nullable = false)
	private String blackName;

	@Column(nullable = false)
	private String result;

	@Column(nullable = false)
	private int plyCount;

	@Column(nullable = false)
	private Instant createdAt;

	@Lob
	@Column(nullable = false)
	private String finalFen;

	@Lob
	@Column(nullable = false)
	private String movesJson;

	@Lob
	@Column(nullable = false)
	private String pgn;

	protected JpaGameEntity() {
	}

	JpaGameEntity(NewGame game, String movesJson) {
		this.whiteName = game.white();
		this.blackName = game.black();
		this.result = game.result();
		this.plyCount = game.plyCount();
		this.createdAt = game.createdAt();
		this.finalFen = game.finalFen();
		this.movesJson = movesJson;
		this.pgn = game.pgn();
	}

	Long id() {
		return id;
	}

	String whiteName() {
		return whiteName;
	}

	String blackName() {
		return blackName;
	}

	String result() {
		return result;
	}

	int plyCount() {
		return plyCount;
	}

	Instant createdAt() {
		return createdAt;
	}

	String finalFen() {
		return finalFen;
	}

	String movesJson() {
		return movesJson;
	}

	String pgn() {
		return pgn;
	}
}
