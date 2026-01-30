package striff.test.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.spi.ClassDecorator;
import com.hadi.striff.spi.ClassInsertionPoint;

import java.util.List;

public class BottomSentinelDecorator implements ClassDecorator {

    @Override
    public ClassInsertionPoint insertionPoint() {
        return ClassInsertionPoint.BOTTOM;
    }

    @Override
    public List<String> decorateClass(DiagramComponent component, DiagramDisplay display) {
        return List.of("BOTTOM_SENTINEL\n");
    }
}
