package com.hadii.striff.diagram;

import com.hadii.clarpse.sourcemodel.Package;

public class ComponentHelper {

    public static String packagePath(Package pkg) {
        if (pkg != null) {
            if (!pkg.ellipsisSeparatedPkgPath().isEmpty()) {
                return pkg.ellipsisSeparatedPkgPath();
            } else {
                return pkg.name();
            }
        } else {
            return "";
        }
    }

}
