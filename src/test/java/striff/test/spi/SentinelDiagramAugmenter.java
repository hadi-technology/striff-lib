package striff.test.spi;

import com.hadii.striff.diagram.DiagramComponent;
import com.hadii.striff.parse.CodeDiff;
import com.hadii.striff.spi.DiagramAugmenter;

import java.util.Set;

public class SentinelDiagramAugmenter implements DiagramAugmenter {

    static final String PROPERTY = "striff.test.enableAugmenter";
    static final String TARGET_COMPONENT = "com.sample.B";

    @Override
    public void augment(CodeDiff diff, Set<DiagramComponent> components) {
        if (!"true".equals(System.getProperty(PROPERTY))) {
            return;
        }
        components.add(new DiagramComponent(TARGET_COMPONENT, diff.mergedModel()));
    }
}
