package edu.uw.beardcl.concurrentbroker;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * An order queue using an executor to dispatch orders.
 *
 * @param <T> the order type contained in the queue
 *
 * @author Chester Beard
 */
public final class ExecutorOrderQueue<T extends Order>
             implements OrderQueue<T>, Runnable {

    /** The queue data structure */
    private TreeSet<T> queue;

    /** The dispatch filter used to determine if an order is dispatchable */
    private OrderDispatchFilter<?, T> filter;

    /** Order processor used to process dispatchable orders */
    private OrderProcessor orderProcessor;

    /** Executor for processing orders. */
    private Executor orderExecutor;

    /** The lock used to control access to the queue */
    private ReentrantLock queueLock = new ReentrantLock();

    /** The lock used to control access to the processor callback object */
    private ReentrantLock processorLock = new ReentrantLock();

    /** Indicator for this runnable's pooled status. */
    private AtomicBoolean isQueuedToPool = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     * @param executor the executor to be used to process this queues orders
     */
    public ExecutorOrderQueue(final OrderDispatchFilter<?, T> filter,
                              final Executor executor) {
        queue = new TreeSet<>();
        orderExecutor = executor;
        this.filter = filter;
        this.filter.setOrderQueue(this);
    }

    /**
     * Constructor.
     *
     * @param cmp Comparator to be used for ordering
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     * @param executor the executor to be used to process this queues orders
     */
    public ExecutorOrderQueue(final Comparator<T> cmp,
                              final OrderDispatchFilter<?, T> filter,
                              final Executor executor) {
        queue = new TreeSet<>(cmp);
        orderExecutor = executor;
        this.filter = filter;
        this.filter.setOrderQueue(this);
    }

    /**
     * Adds the specified order to the queue.
     *
     * @param order the order to be added to the queue
     */
    public void enqueue(final T order) {
        queueLock.lock();
        try {
            queue.add(order);
        } finally {
            queueLock.unlock();
        }
        dispatchOrders();
    }

    /**
     * Removes the highest dispatchable order in the queue. If there are orders
     * in the queue but they do not meet the dispatch threshold no order will
     * be removed and null will be returned.
     *
     * @return the highest order in the queue, or null if there are no
     *         dispatchable orders in the queue
     */
    public T dequeue() {
        T order = null;
        queueLock.lock();
        try {
            if (!queue.isEmpty()) {
                order = queue.first();

                if ((filter != null) && !filter.check(order)) {
                    order = null;
                } else {
                    queue.remove(order);
                }
            }
        } finally {
            queueLock.unlock();
        }

        return order;
    }

    /**
     * Signals the waiting dispatch thread to process orders.
     */
    public void dispatchOrders() {
        queueLock.lock();
        try {
            if (isQueuedToPool.compareAndSet(false, true)) {
                orderExecutor.execute(this);
            }
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Dispatch orders as long as there are dispatchable orders available.
     */
    public void run() {
        while (true) {
            T order;

            queueLock.lock();
            try {
                order = dequeue();
                if (order == null) {
                    isQueuedToPool.set(false);
                    break;
                }
            } finally {
                queueLock.unlock();
            }

            processorLock.lock();
            try {
                if (orderProcessor != null) {
                    orderProcessor.process(order);
                }
            } finally {
                processorLock.unlock();
            }
            // or...
            // OrderProcessor op = orderProcessor;
            // if (op != null) {
            //    op.process(order);
            // }
        }
    }

    /**
     * Registers the callback to be invoked during order processing.
     *
     * @param proc the callback to be registered
     */
    public void setOrderProcessor(final OrderProcessor proc) {
        processorLock.lock();
        try {
            orderProcessor = proc;
        } finally {
            processorLock.unlock();
        }
        // or...
        // orderProcessor = proc;
    }
}

