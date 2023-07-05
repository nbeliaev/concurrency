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
            if (latestBid.isMarked()) {
                return false;
            }

            currentBid = latestBid.getReference();
            if (bid.getPrice() <= currentBid.getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(currentBid, bid, false, false));

        notifier.sendOutdatedMessage(currentBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        if (latestBid.isMarked()) {
            return latestBid.getReference();
        }
        latestBid.set(latestBid.getReference(), true);
        return latestBid.getReference();
    }
}
