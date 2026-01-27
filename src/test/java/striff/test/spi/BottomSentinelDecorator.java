package striff.test.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.spi.ClassDecorator;
import com.hadii.striff.spi.ClassInsertionPoint;

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
