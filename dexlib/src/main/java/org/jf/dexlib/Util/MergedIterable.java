package org.jf.dexlib.Util;

import java.util.Iterator;

/**
 * This class may be used to merge two iterable object into a single one.
 * The order of the elements is preserved. 
 *
 * @param <E> Type of the elements contained in the iterable objects.
 */
public class MergedIterable<E> implements Iterable<E> {

    private final Iterable<E> first;
    private final Iterable<E> second;
    
    /**
     * Creates a new instance of a merged iterable. Combines two given iterables.
     * Order is preserved. The iterator will iterate over all elements of the
     * first parameter and continue with all elements of the second one. The parameters
     * may not be null.
     * @param first The first iterable to be merged.
     * @param second The second iterable to be merged.
     */
    public MergedIterable(final Iterable<E> first, final Iterable<E> second) {
        if (first == null) {
            throw new IllegalArgumentException("first list is null");
        } else if (second == null) {
            throw new IllegalArgumentException("second list is null");
        }
        
        this.first = first;
        this.second = second;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<E> iterator() {
        return new MergedIterator();
    }
    
    private class MergedIterator implements Iterator<E> {

        private final Iterator<E> firstIt = first.iterator();
        private final Iterator<E> secondIt = second.iterator();
        
        public boolean hasNext() {
            return firstIt.hasNext() || secondIt.hasNext();
        }

        public E next() {
            return (firstIt.hasNext() ? firstIt.next() : secondIt.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}