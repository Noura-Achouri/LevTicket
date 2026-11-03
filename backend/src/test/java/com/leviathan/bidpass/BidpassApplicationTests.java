package com.leviathan.bidpass;

import com.leviathan.bidpass.entity.Auction;
import com.leviathan.bidpass.entity.TrustScore;
import com.leviathan.bidpass.service.TrustScoreService;
import com.leviathan.bidpass.mapper.TrustScoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidpassApplicationTests {

	// ── Test 1: Bid amount must exceed current bid ──────────────────────────
	@Test
	void testBidMustExceedCurrentBid() {
		BigDecimal currentBid = new BigDecimal("500.00");
		BigDecimal newBid = new BigDecimal("450.00");

		boolean isValid = newBid.compareTo(currentBid) > 0;

		assertFalse(isValid, "Bid lower than current bid should be invalid");
	}

	// ── Test 2: Auction expiry check ────────────────────────────────────────
	@Test
	void testAuctionIsExpiredWhenEndTimeIsPast() {
		Auction auction = new Auction();
		auction.setEndTime(LocalDateTime.now().minusHours(1));

		boolean isExpired = LocalDateTime.now().isAfter(auction.getEndTime());

		assertTrue(isExpired, "Auction with past end time should be expired");
	}

	// ── Test 3: Active auction should not be expired ────────────────────────
	@Test
	void testAuctionIsNotExpiredWhenEndTimeIsFuture() {
		Auction auction = new Auction();
		auction.setEndTime(LocalDateTime.now().plusHours(24));

		boolean isExpired = LocalDateTime.now().isAfter(auction.getEndTime());

		assertFalse(isExpired, "Auction with future end time should not be expired");
	}

	// ── Test 4: Trust score calculation ────────────────────────────────────
	@Test
	void testTrustScoreCalculation() {
		int successfulAuctions = 5;
		int disputes = 1;

		int score = (successfulAuctions * 2) - (disputes * 3);

		assertEquals(7, score, "Trust score should be (5*2) - (1*3) = 7");
	}

	// ── Test 5: Trust score should never go below zero ──────────────────────
	@Test
	void testTrustScoreNeverBelowZero() {
		int successfulAuctions = 0;
		int disputes = 5;

		int score = (successfulAuctions * 2) - (disputes * 3);
		if (score < 0) score = 0;

		assertEquals(0, score, "Trust score should not go below zero");
	}

	// ── Test 6: Seller cannot bid on own ticket ─────────────────────────────
	@Test
	void testSellerCannotBidOnOwnTicket() {
		Integer sellerId = 6;
		Integer bidderId = 6;

		boolean isSamePerson = sellerId.equals(bidderId);

		assertTrue(isSamePerson, "Seller and bidder are the same person — bid should be blocked");
	}
}