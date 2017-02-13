package edu.uw.beardcl.broker;

import java.util.Comparator;

import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Comparator which places the orders in descending order - the highest priced
 * order is the first item in the list.
 *
 * @author Chester Beard
 */
public final class StopSellOrderComparator implements Comparator<StopSellOrder> {

    /**
     * Performs the comparison.
     *
     * @param order1 first of two orders to be compared
     * @param order2 first of two orders to be compared
     *
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     *         Where the lesser order has the the higher price, if prices are
     *         equal the lesser order will have the highest order quantity,
     *         finally if price and quantity are equal the lesser order will
     *         have the lower order id.
     */
    public int compare(final StopSellOrder order1, final StopSellOrder order2) {
    	if (order1 == order2) {
    		return 0;
    	}
        int diff = Integer.compare(order2.getPrice(), order1.getPrice());
        if (diff == 0) {
            diff = order1.compareTo(order2);
        }

        return diff;
    }
}

