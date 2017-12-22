package com.clarity.binary;

import com.clarity.sourcemodel.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of components.
 */
public class MergedSourceCodeModel {

    private Map<String, Component> componentSet;

    /**
     * Merges two source code models, gives preference to the newer model.
     */
    public MergedSourceCodeModel(Map<String, Component> olderModel, Map<String, Component> newerModel) {

        Map<String, Component> newModelClone = new HashMap<>(newerModel);
        // inefficient way to merge the given sets of components..
        for (Component oldCmp : olderModel.values()) {
            boolean existsInNewerSet = false;
            if (newerModel.containsKey(oldCmp.uniqueName())) {
                Component newCmp = newerModel.get(oldCmp.uniqueName());
                    existsInNewerSet = true;
                    // merge the old components children into the newer set
                    for (String olderCmpChild : oldCmp.children()) {
                        if (!newCmp.children().contains(olderCmpChild)) {
                            newCmp.children().add(olderCmpChild);
                        }
                    }

            }
            if (!existsInNewerSet) {
                newModelClone.put(oldCmp.uniqueName(), oldCmp);
            }
        }
        this.componentSet = newModelClone;
    }

    public Map<String, Component> set() {
        return this.componentSet;
    }
}
