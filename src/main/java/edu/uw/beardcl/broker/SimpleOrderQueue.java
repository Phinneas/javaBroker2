package edu.uw.beardcl.broker;

import java.util.Comparator;
import java.util.TreeSet;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A simple OrderQueue implementation backed by a TreeSet.
 *
 * @param <E> the type of order contained in the queue
 *
 * @author Chester Beard
 */
public final class SimpleOrderQueue<E extends Order>
                                      implements OrderQueue<E> {
    /** The queue data structure */
    private TreeSet<E> queue;

    /** The dispatch filter used to determine if an order is dispatchable */
    private OrderDispatchFilter<?, E> filter;

    /** Order processor used to process dispatchable orders */
    private OrderProcessor orderProcessor;

    /**
     * Constructor.
     *
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     */
    public SimpleOrderQueue(final OrderDispatchFilter<?, E> filter) {
        queue = new TreeSet<>();
        this.filter = filter;
        this.filter.setOrderQueue(this);
    }

    /**
     * Constructor.
     *
     * @param cmp Comparator to be used for ordering
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     */
    public SimpleOrderQueue(final Comparator<E> cmp,
                            final OrderDispatchFilter<?, E> filter) {
        queue = new TreeSet<>(cmp);
        this.filter = filter;
        this.filter.setOrderQueue(this);
    }

    /**
     * Adds the specified order to the queue.  Subsequent to adding the order
     * dispatches any dispatchable orders.
     *
     * @param order the order to be added to the queue
     */
    public void enqueue(final E order) {
        queue.add(order);
        dispatchOrders();
    }

    /**
     * Removes the highest dispatchable order in the queue. If there are orders
     * in the queue but they do not meet the dispatch threshold order will not
     * be removed and null will be returned.
     *
     * @return the first dispatchable order in the queue, or null if there are no
     *         dispatchable orders in the queue
     */
    public E dequeue() {
        E order = null;

        if (!queue.isEmpty()) {
            order = queue.first();

            if (filter.check(order)) {
                queue.remove(order);
            } else {
                order = null;
            }
        }

        return order;
    }

    /**
     * Executes the orderProcessor for each dispatchable order.  Each dispatchable
     * order is in turn removed from the queue and passed to the callback.  If
     * no callback is registered the order is simply removed from the queue.
     */
    public void dispatchOrders() {
        E order;

        while ((order = dequeue()) != null) {
            if (orderProcessor != null) {
                orderProcessor.process(order);
            }
        }
    }

    /**
     * Registers the callback to be used during order processing.
     *
     * @param proc the callback to be registered
     */
    public void setOrderProcessor(final OrderProcessor proc) {
        orderProcessor = proc;
    }
}

