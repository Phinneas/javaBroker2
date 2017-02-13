package edu.uw.beardcl.concurrentbroker;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * A multi-threaded order queue, a separate thread is used to dispatch orders.
 *
 * @param <T> the order type contained in the queue
 *
 * @author Chester Beard
 */
public final class ThreadedOrderQueue<T extends Order>
             implements OrderQueue<T>, Runnable {

    /** The queue data structure */
    private TreeSet<T> queue;

    /** The dispatch filter used to determine if an order is dispatchable */
    private OrderDispatchFilter<?, T> filter;

    /** Order processor used to process dispatchable orders */
    private OrderProcessor orderProcessor;

    /** Thread responsible for dispatching orders */
    private Thread dispatchThread;

    /** The lock used to control access to the queue */
    private final ReentrantLock queueLock = new ReentrantLock();

    /** Condition used to initiate processing orders. */
    private final Condition dispatchCondition = queueLock.newCondition();

    /** The lock used to control access to the processor callback object */
    private final ReentrantLock processorLock = new ReentrantLock();

    /**
     * Constructor.
     *
     * @param name the name of this order queue
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     */
    public ThreadedOrderQueue(final String name, final OrderDispatchFilter<?, T> filter) {
        queue = new TreeSet<>();
        this.filter = filter;
        this.filter.setOrderQueue(this);
        startDispatchThread(name);
    }

    /**
     * Constructor.
     *
     * @param name the name of this order queue
     * @param cmp Comparator to be used for ordering
     * @param filter the dispatch filter used to control dispatching from this
     *               queue
     */
    public ThreadedOrderQueue(final String name, final Comparator<T> cmp,
                              final OrderDispatchFilter<?, T> filter) {
        queue = new TreeSet<>(cmp);
        this.filter = filter;
        this.filter.setOrderQueue(this);
        startDispatchThread(name);
    }

    /**
     * Sets the priority of the order queue.
     *
     * @param priority the priority for the  order queue
     */
    public void setPriority(final int priority) {
        dispatchThread.setPriority(priority);
    }

    /**
     * Creates and starts a dispatch thread.
     *
     * @param name the name of this order queue, to be assigned to the thread
     */
    private void startDispatchThread(final String name) {
        dispatchThread = new Thread(this, name + "-OrderDispatchThread");
        dispatchThread.setDaemon(true);
        dispatchThread.start();
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
     * in the queue but they do not meet the dispatch threshold not order will
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
            dispatchCondition.signal();
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
                while ((order = dequeue()) == null) {
                    try {
                        dispatchCondition.await();
                    } catch (final InterruptedException iex) {
                        final Logger log = LoggerFactory.getLogger(this.getClass());
                        log.info("Order queue interrupted while waiting");
                    }
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

