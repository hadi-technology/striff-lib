package striff.test.spi;

import com.hadi.striff.spi.PackageDecorator;
import com.hadi.striff.spi.PackageDecoratorContext;

import java.util.List;

public class PackageSentinelDecorator implements PackageDecorator {

    @Override
    public List<String> decoratePackage(PackageDecoratorContext context) {
        if (!"com.sample.b".equals(context.packagePath())) {
            return List.of();
        }
        return List.of("note right of B", "PACKAGE_SENTINEL", "end note");
    }
}
