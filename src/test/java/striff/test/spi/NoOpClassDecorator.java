package striff.test.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.diagram.display.DiagramDisplay;
import com.hadii.striff.spi.ClassDecorator;
import com.hadii.striff.spi.ClassInsertionPoint;

import java.util.List;

public class NoOpClassDecorator implements ClassDecorator {

    @Override
    public ClassInsertionPoint insertionPoint() {
        return ClassInsertionPoint.TOP;
    }

    @Override
    public List<String> decorateClass(DiagramComponent component, DiagramDisplay display) {
        return List.of();
    }
}
