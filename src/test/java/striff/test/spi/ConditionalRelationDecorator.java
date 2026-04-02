package striff.test.spi;

import com.hadi.striff.diagram.display.DiagramDisplay;
import com.hadi.striff.extractor.ComponentRelation;
import com.hadi.striff.spi.RelationDecorator;

import java.util.List;

public class ConditionalRelationDecorator implements RelationDecorator {
    @Override
    public List<String> decorateRelation(ComponentRelation relation, ComponentRelation reverseRelation,
            DiagramDisplay display) {
        if ("DecorationSource".equals(relation.originalComponent().name())
                && "DecorationTarget".equals(relation.targetComponent().name())) {
            return List.of("' relation-decoration");
        }
        return List.of();
    }
}
