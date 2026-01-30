package striff.test.spi;

import com.hadi.striff.diagram.DiagramComponent;
import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.spi.ClassDecorator;
import com.hadi.striff.spi.ClassInsertionPoint;

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
