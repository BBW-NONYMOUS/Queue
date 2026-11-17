package ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.List;

public final class OrderedFocusTraversalPolicy extends FocusTraversalPolicy {
    private final List<Component> order;

    public OrderedFocusTraversalPolicy(Component... components) {
        order = List.of(components);
    }

    private Component next(Component current, int direction) {
        // List.of(...) rejects indexOf(null); Swing passes null when no component owns focus yet.
        int index = current == null ? -1 : order.indexOf(current);
        for (int step = 1; step <= order.size(); step++) {
            Component candidate = order.get(Math.floorMod(index + step * direction, order.size()));
            if (candidate.isShowing() && candidate.isEnabled() && candidate.isFocusable()) {
                return candidate;
            }
        }
        return null;
    }

    @Override public Component getComponentAfter(Container root, Component current) { return next(current, 1); }
    @Override public Component getComponentBefore(Container root, Component current) { return next(current, -1); }
    @Override public Component getFirstComponent(Container root) { return next(null, 1); }
    @Override public Component getLastComponent(Container root) { return next(order.getFirst(), -1); }
    @Override public Component getDefaultComponent(Container root) { return getFirstComponent(root); }
}
