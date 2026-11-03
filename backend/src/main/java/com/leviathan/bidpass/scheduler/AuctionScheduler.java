package com.leviathan.bidpass.scheduler;

import com.leviathan.bidpass.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps auctions moving without any user interaction: every minute it asks the
 * auction service to settle any auction whose end time has passed.
 */
@Component
public class AuctionScheduler {

    @Autowired
    private AuctionService auctionService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    public void settleExpiredAuctions() {
        try {
            int closed = auctionService.closeExpiredAuctions();
            if (closed > 0) {
                System.out.println("[AuctionScheduler] auto-settled " + closed + " expired auction(s)");
            }
        } catch (Exception e) {
            System.err.println("[AuctionScheduler] run failed: " + e.getMessage());
        }
    }
}
