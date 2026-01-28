/**
 * Simplified Rejected Order (Optional - basic rejection tracking in SharedResources)
 */
public class RejectedOrder {
    private final int orderId;
    private final String reason;
    private final String stage;
    
    public RejectedOrder(int orderId, String stage, String reason) {
        this.orderId = orderId;
        this.stage = stage;
        this.reason = reason;
    }
    
    public int getOrderId() { return orderId; }
    public String getReason() { return reason; }
    public String getStage() { return stage; }
    
    @Override
    public String toString() {
        return "RejectedOrder #" + orderId + " at " + stage + " for " + reason;
    }
}