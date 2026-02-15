import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Container entity
 */
public class Container {
    private final int containerId;
    private final List<Box> boxes;
    private boolean sealed;
    
    public Container(int containerId) {
        this.containerId = containerId;
        this.boxes = new ArrayList<>();
        this.sealed = false;
    }
    
    public synchronized boolean addBox(Box box) {
        if (sealed || boxes.size() >= Constants.BOXES_PER_CONTAINER) {
            return false;
        }
        boxes.add(box);
        return true;
    }
    
    public synchronized void seal() {
        this.sealed = true;
    }
    
    public boolean isFull() {
        return boxes.size() >= Constants.BOXES_PER_CONTAINER;
    }
    
    public int getBoxCount() { return boxes.size(); }
    public int getContainerId() { return containerId; }
    public boolean isSealed() { return sealed; }
    
    @Override
    public String toString() {
        return "Container #" + containerId + " [Boxes:" + boxes.size() + 
               "/" + Constants.BOXES_PER_CONTAINER + ", Sealed:" + sealed + "]";
    }
}