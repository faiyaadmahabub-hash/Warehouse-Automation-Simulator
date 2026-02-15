import java.util.Random;

/**
 * Simplified AGV Failure Simulator
 */
public class AGVFailureSimulator extends Thread {
    private final Random random = new Random();
    private final Statistics statistics; // Add statistics reference
    private int breakdownsSimulated = 0;
    
    public AGVFailureSimulator(Statistics statistics) {
        super("AGVFailureSimulator-1");
        this.setDaemon(true);
        this.statistics = statistics; // Store statistics reference
    }
    
    @Override
    public void run() {
        System.out.println("AGVFailureSimulator: Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning()) {
                // Check each AGV for breakdown
                for (int agvId = 0; agvId < Constants.MAX_AGVS; agvId++) {
                    if (SharedResources.isAGVOperational(agvId) && 
                        random.nextDouble() < Constants.AGV_BREAKDOWN_PROBABILITY) {
                        simulateBreakdown(agvId);
                    }
                }
                
                Thread.sleep(random.nextInt(3000) + 2000); // 2-5 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("AGVFailureSimulator: Completed - " + breakdownsSimulated + 
                          " breakdowns simulated (Thread: " + getName() + ")");
    }
    
    private void simulateBreakdown(int agvId) {
        long breakdownStartTime = System.currentTimeMillis(); // Track breakdown time
        
        SharedResources.breakdownAGV(agvId);
        breakdownsSimulated++;
        statistics.recordAGVBreakdown(); // Track in statistics
        
        System.out.println("AGVFailureSimulator: AGV-" + (agvId + 1) + 
                          " breakdown simulated (Thread: " + getName() + ")");
        
        // Schedule repair
        Thread repairThread = new Thread(() -> {
            try {
                Thread.sleep(Constants.AGV_RECOVERY_TIME);
                SharedResources.repairAGV(agvId);
                
                long downtimeMs = System.currentTimeMillis() - breakdownStartTime;
                statistics.addAGVDowntime(downtimeMs); // Track downtime in statistics
                
                System.out.println("AGVFailureSimulator: AGV-" + (agvId + 1) + 
                                 " repaired (Thread: " + Thread.currentThread().getName() + ")");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        repairThread.setName("AGVRepair-" + (agvId + 1));
        repairThread.setDaemon(true);
        repairThread.start();
    }
    
    public void forceRecoverAllAGVs() {
        for (int agvId = 0; agvId < Constants.MAX_AGVS; agvId++) {
            if (!SharedResources.isAGVOperational(agvId)) {
                SharedResources.repairAGV(agvId);
            }
        }
    }
    
    public int getTotalBreakdownsSimulated() { return breakdownsSimulated; }
}