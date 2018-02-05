package com.clarity.binary;

import com.clarity.binary.diagram.DiagramComponent;

import java.util.Iterator;
import java.util.Map;

/**
 * General utility related to the Clarity Web Service.
 */
public final class ClarityUtil {

    private ClarityUtil() {

    }

    public static DiagramComponent getParentBaseComponent(DiagramComponent cmp, Map<String, DiagramComponent> map) {
        String currParentClassName = cmp.parentUniqueName();

        DiagramComponent parent;
        for (parent = map.get(currParentClassName); parent != null && !parent.componentType().isBaseComponent(); parent = map.get(currParentClassName)) {
            currParentClassName = parent.parentUniqueName();
        }

        return parent;
    }

    public static Object getObjectFromStringObjectKeyValueMap(String value, Map<?, ?> hashMap) {
        final Iterator<?> it = hashMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<?, ?> pair = (Map.Entry<?, ?>) it.next();
            if (pair.getValue().equals(value)) {
                return pair.getKey();
            }
        }
        return null;
    }

    public enum InvocationSiteProperty {

        FIELD, LOCAL, NONE, METHOD_PARAMETER, CONSTRUCTOR_PARAMETER
    }

}
