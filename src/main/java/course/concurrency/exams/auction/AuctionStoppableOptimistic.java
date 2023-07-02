package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(new Bid(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE), false);

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        Bid currentBid;
        do {
            currentBid = latestBid.getReference();
            if (bid.getPrice() <= currentBid.getPrice()) {
                return false;
            }
        } while (!latestBid.isMarked() && !latestBid.compareAndSet(currentBid, bid, latestBid.isMarked(), latestBid.isMarked()));

        notifier.sendOutdatedMessage(currentBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        while (!latestBid.attemptMark(latestBid.getReference(), true)) {

        }
        return latestBid.getReference();
    }
}
