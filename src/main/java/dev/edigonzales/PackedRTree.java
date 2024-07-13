package dev.edigonzales;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.locationtech.jts.geom.Envelope;

public class PackedRTree {
    private static final int NODE_ITEM_LEN = 8 * 4 + 8;

    private static final String ILLEGAL_NODE_SIZE = "Node size must be at least 2";
    private static final String ILLEGAL_NUMBER_OF_ITEMS = "Number of items must be greater than 0";

    public static long calcSize(long numItems, int nodeSize) {
        if (nodeSize < 2) {
            throw new IllegalArgumentException(ILLEGAL_NODE_SIZE);
        }
        if (numItems == 0) {
            throw new IllegalArgumentException(ILLEGAL_NUMBER_OF_ITEMS);
        }
        int nodeSizeMin = Math.min(nodeSize, 65535);
        // limit so that resulting size in bytes can be represented by ulong
        if (numItems > 1L << 56) {
            throw new IndexOutOfBoundsException("Number of items must be less than 2^56");
        }
        long n = numItems;
        long numNodes = n;
        do {
            n = (n + nodeSizeMin - 1) / nodeSizeMin;
            numNodes += n;
        } while (n != 1);
        return numNodes * NODE_ITEM_LEN;
    }

    
    public static ArrayList<IndexItem> getIndex(ByteBuffer bb, int start, int numItems, int nodeSize, Envelope rect) {
        ArrayList<IndexItem> indexItems = new ArrayList<>();
        ArrayList<SearchHit> searchHits = new ArrayList<SearchHit>();
        double minX = rect.getMinX();
        double minY = rect.getMinY();
        double maxX = rect.getMaxX();
        double maxY = rect.getMaxY();
        List<Pair<Integer, Integer>> levelBounds = generateLevelBounds(numItems, nodeSize);
        int leafNodesOffset = levelBounds.get(0).first;
        int numNodes = levelBounds.get(0).second;
        Deque<QueueItem> queue = new LinkedList<QueueItem>();
        queue.add(new QueueItem(0, levelBounds.size() - 1));
        while (queue.size() != 0) {
            QueueItem stackItem = queue.pop();
            int nodeIndex = (int) stackItem.nodeIndex;
            int level = stackItem.level;
            boolean isLeafNode = nodeIndex >= numNodes - numItems;
            // find the end index of the node
            int levelEnd = levelBounds.get(level).second;
            int end = Math.min(nodeIndex + nodeSize, levelEnd);
            int nodeStart = start + (nodeIndex * NODE_ITEM_LEN);
            // int length = end - nodeIndex;
            // search through child nodes
            for (int pos = nodeIndex; pos < end; pos++) {
                
                System.out.println("pos: " + pos);
                
//                if (isLeafNode) {
////                    indexItems.add(new IndexItem(indexOffet))                    
//                }
                
                int offset = nodeStart + ((pos - nodeIndex) * NODE_ITEM_LEN);
//                double nodeMinX = bb.getDouble(offset + 0);
//                double nodeMinY = bb.getDouble(offset + 8);
//                double nodeMaxX = bb.getDouble(offset + 16);
//                double nodeMaxY = bb.getDouble(offset + 24);
//                if (maxX < nodeMinX)
//                    continue;
//                if (maxY < nodeMinY)
//                    continue;
//                if (minX > nodeMaxX)
//                    continue;
//                if (minY > nodeMaxY)
//                    continue;
                long indexOffset = bb.getLong(offset + 32);
                if (isLeafNode) {
//                    searchHits.add(new SearchHit(indexOffset, pos - leafNodesOffset));
                    indexItems.add(new IndexItem(indexOffset, pos - leafNodesOffset));
                } else {
                    queue.add(new QueueItem(indexOffset, level - 1));                    
                }
            }
        }
        return indexItems;
    }
    
    static List<Pair<Integer, Integer>> generateLevelBounds(int numItems, int nodeSize) {
        if (nodeSize < 2)
            throw new RuntimeException("Node size must be at least 2");
        if (numItems == 0)
            throw new RuntimeException("Number of items must be greater than 0");

        // number of nodes per level in bottom-up order
        int n = numItems;
        int numNodes = n;
        ArrayList<Integer> levelNumNodes = new ArrayList<Integer>();
        levelNumNodes.add(n);
        do {
            n = (n + nodeSize - 1) / nodeSize;
            numNodes += n;
            levelNumNodes.add(n);
        } while (n != 1);

        // offsets per level in reversed storage order (top-down)
        ArrayList<Integer> levelOffsets = new ArrayList<Integer>();
        n = numNodes;
        for (int size : levelNumNodes)
            levelOffsets.add(n -= size);
        List<Pair<Integer, Integer>> levelBounds = new LinkedList<>();
        // bounds per level in reversed storage order (top-down)
        for (int i = 0; i < levelNumNodes.size(); i++)
            levelBounds.add(new Pair<>(levelOffsets.get(i), levelOffsets.get(i) + levelNumNodes.get(i)));
        return levelBounds;
    }
    
    public static class IndexItem {
        public IndexItem(long offset, long index) {
            this.offset = offset;
            this.index = index;
        }
        public long offset;
        public long index;
    }
    
    public static class SearchHit {
        public SearchHit(long offset, long index) {
            this.offset = offset;
            this.index = index;
        }

        public long offset;
        public long index;
    }
    
    private static class QueueItem {
        public QueueItem(long nodeIndex, int level) {
            this.nodeIndex = nodeIndex;
            this.level = level;
        }

        long nodeIndex;
        int level;
    }    
    
    static class Pair<T, U> {
        public T first;
        public U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

}
