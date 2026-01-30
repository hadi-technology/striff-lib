package com.hadi.striff.spi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.ToIntFunction;

public final class SpiLoader {

    private SpiLoader() {
    }

    public static <T> List<T> loadOrdered(Class<T> spi, ToIntFunction<T> orderFn) {
        List<T> list = new ArrayList<>();
        ServiceLoader.load(spi).forEach(list::add);
        list.sort(Comparator.comparingInt(orderFn).thenComparing(o -> o.getClass().getName()));
        return list;
    }
}
