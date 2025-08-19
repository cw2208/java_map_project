package maps;

//import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
//import java.util.Objects;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {
    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 1;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 5;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 3;
    private int size;
    private double resizingLoadFactorThreshold;
    private int chainInitialCapacity;
    private Iterator<Map.Entry<K, V>> iter;


    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    AbstractIterableMap<K, V>[] chains;

    // You're encouraged to add extra fields (and helper methods) though!

    /**
     * Constructs a new ChainedHashMap with default resizing load factor threshold,
     * default initial chain count, and default initial chain capacity.
     */
    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    /**
     * Constructs a new ChainedHashMap with the given parameters.
     *
     * @param resizingLoadFactorThreshold the load factor threshold for resizing. When the load factor
     *                                    exceeds this value, the hash table resizes. Must be > 0.
     * @param initialChainCount the initial number of chains for your hash table. Must be > 0.
     * @param chainInitialCapacity the initial capacity of each ArrayMap chain created by the map.
     *                             Must be > 0.
     */
    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        this.chains = createArrayOfChains(initialChainCount);
        this.resizingLoadFactorThreshold = resizingLoadFactorThreshold;
        this.chainInitialCapacity = chainInitialCapacity;
        size = 0;

        //throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    /**
     * Returns a new chain.
     *
     * This method will be overridden by the grader so that your ChainedHashMap implementation
     * is graded using our solution ArrayMaps.
     *
     * Note: You do not need to modify this method.
     */
    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    private int hash(Object key) {
        if (key == null) {
            return 0;
        }
        return Math.abs(key.hashCode()) % chains.length;
    }

    @Override
    public V get(Object key) {
        int index = hash(key);
        if (chains[index] == null) {
            return null;
        }
        return chains[index].get(key);
    }

    private void resize() {
        AbstractIterableMap<K, V>[] oldMap = chains;
        chains = createArrayOfChains(chains.length * 2);
        size = 0;

        for (AbstractIterableMap<K, V> chain : oldMap) {
            if (chain != null) {
                for (Map.Entry<K, V> bucket : chain) {
                    put(bucket.getKey(), bucket.getValue());
                }
            }
        }
    }


    @Override
    public V put(K key, V value) {
        if ((double) size / chains.length >= resizingLoadFactorThreshold) {
            resize();
        }
        int index = hash(key);


        if (chains[index] == null) {
            chains[index] = createChain(chainInitialCapacity);
        }
        if (!chains[index].containsKey(key)) {
            size++;
        }
        return chains[index].put(key, value);

    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            return null;
        }
        int index = hash(key);
        if (chains[index] == null) {
            return null;
        }
        if (chains[index].containsKey(key)) {
            size--;
        }
        return chains[index].remove(key);
    }

    @Override
    public void clear() {
        size = 0;
        chains = createArrayOfChains(chains.length);
    }

    @Override
    public boolean containsKey(Object key) {
        int index = hash(key);
        if (chains[index] != null && chains[index].containsKey(key)) {
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ChainedHashMapIterator<>(this.chains);
    }


    // Doing so will give you a better string representation for assertion errors the debugger.
    @Override
    public String toString() {
        return super.toString();
    }

    /*
    See the assignment webpage for tips and restrictions on implementing this iterator.
     */
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        private int outerCurr;
        private Iterator<Map.Entry<K, V>> iter;

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains) {
            this.outerCurr = 0;
            this.chains = chains;
            this.iter = null;


            if (chains[outerCurr] != null) {
                iter = chains[outerCurr].iterator();
            }
            while (iter == null && outerCurr < chains.length - 1) {
                outerCurr++;
                if (chains[outerCurr] != null) {
                    iter = chains[outerCurr].iterator();
                }
            }
        }

        private void advanceIterator() {
            while ((iter == null || !iter.hasNext()) && outerCurr < chains.length - 1) {
                outerCurr++;
                if (chains[outerCurr] != null) {
                    iter = chains[outerCurr].iterator();
                }
            }
        }

        @Override
        public boolean hasNext() {
            advanceIterator();
            return iter != null && iter.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map.Entry<K, V> entry = iter.next();
            // if(!iter.hasNext()) {
            //     advanceIterator();
            // }
            return entry;
        }
    }
}
