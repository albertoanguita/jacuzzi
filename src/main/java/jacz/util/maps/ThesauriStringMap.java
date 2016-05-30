package jacz.util.maps;

import jacz.util.sets.DuplicateHashSet;
import jacz.util.string.StringBuilderPool;

import java.util.*;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 2/08/11<br>
 * Last Modified: 2/08/11
 */
public class ThesauriStringMap<E> implements Map<String, E> {

    public static class Node<E> implements Comparable<Node> {

        private Node<E> parentNode;

        private String key;

        private E value;

        private boolean isLeaf;

        private int childrenSize;

        private List<Node<E>> children;

        public Node(Node<E> parent, String key) {
            this.parentNode = parent;
            this.key = key;
            this.isLeaf = false;
            this.value = null;
            children = new ArrayList<>();
            childrenSize = 0;
        }

        public Node(Node<E> parent, String key, E value) {
            this.parentNode = parent;
            this.key = key;
            isLeaf = true;
            this.value = value;
            children = new ArrayList<>();
            childrenSize = 0;
        }

        @Override
        public int compareTo(Node o) {
            return key.compareTo(o.key);
        }
    }

    private static class SearchResult {

        private boolean found;

        private int index;

        private StringBuilder commonRoot;

        private StringBuilder restToSearch;

        private SearchResult(boolean found, int index, StringBuilder commonRoot, StringBuilder restToSearch) {
            this.found = found;
            this.index = index;
            this.commonRoot = commonRoot;
            this.restToSearch = restToSearch;
        }

        @Override
        public String toString() {
            return "SearchResult: found=" + found + ", index=" + index + ", commonRoot=" + commonRoot + ", restToSearch=" + restToSearch;
        }
    }

    private static class ThesauriStringMapIterator<E> implements Iterator<String> {

        private Node<E> currentNode;

        private StringBuilder currentKey;

        private ArrayList<Integer> path;

        private ThesauriStringMapIterator(ThesauriStringMap<E> map) {
            currentNode = map.rootNode;
            currentKey = new StringBuilder(EMPTY_STRING);
            path = new ArrayList<>();
            if (!currentNode.isLeaf) {
                nextLeafNode();
            }
        }

        @Override
        public boolean hasNext() {
            // returns true if current node points to a valid node
            return currentNode != null;
        }

        @Override
        public String next() {
            if (hasNext()) {
                String ret = currentKey.toString();
                nextLeafNode();
                return ret;
            } else {
                throw new NoSuchElementException("The iterator contains no more elements");
            }
        }

        private void nextLeafNode() {
            do {
                nextNode();
            } while (currentNode != null && !currentNode.isLeaf);
        }

        private void nextNode() {
            // if we can go down, we go down one level
            // if we cannot go down, we go right 1 step
            // if we cannot go right, we go up and right
            if (canMoveDown()) {
                moveDown();
            } else if (canMoveRight()) {
                moveRight();
            } else {
                while (!canMoveRight() && !isRootNode()) {
                    moveUp();
                }
                if (!isRootNode()) {
                    moveRight();
                } else {
                    // finished all moves
                    currentNode = null;
                }
            }
        }

        private boolean isRootNode() {
            return currentNode.key.length() == 0;
        }

        private boolean canMoveDown() {
            return !currentNode.children.isEmpty();
        }

        private void moveDown() {
            currentNode = currentNode.children.get(0);
            currentKey.append(currentNode.key);
            path.add(0);
        }

        private boolean canMoveRight() {
            if (isRootNode()) {
                // the root node cannot move right
                return false;
            } else {
                int index = path.get(path.size() - 1) + 1;
                return index < currentNode.parentNode.children.size();
            }
        }

        private void moveRight() {
            int strToEraseLength = currentNode.key.length();
            int index = path.get(path.size() - 1) + 1;
            currentNode = currentNode.parentNode.children.get(index);
            currentKey.replace(currentKey.length() - strToEraseLength, currentKey.length(), currentNode.key);
            path.set(path.size() - 1, index);
        }

        private void moveUp() {
            currentKey.delete(currentKey.length() - currentNode.key.length(), currentKey.length());
            currentNode = currentNode.parentNode;
            path.remove(path.size() - 1);
        }

        @Override
        public void remove() {
            // ignore
        }
    }

    private static class ThesauriStringMapEntry<E> implements Entry<String, E> {

        private Map<String, E> map;

        private String key;

        private ThesauriStringMapEntry(Map<String, E> map, String key) {
            this.map = map;
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public E getValue() {
            return map.get(key);
        }

        @Override
        public E setValue(E value) {
            return map.put(key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ThesauriStringMapEntry that = (ThesauriStringMapEntry) o;

            if (!key.equals(that.key)) return false;
            //noinspection RedundantIfStatement
            if (!map.equals(that.map)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = map.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }
    }

    private static class PathResult<E> {

        private int lastPath;

        private Node<E> reachedNode;

        private int similarChars;

        private StringBuilder restToSearch;

        private PathResult(int lastPath, Node<E> reachedNode, int similarChars, StringBuilder restToSearch) {
            this.lastPath = lastPath;
            this.reachedNode = reachedNode;
            this.similarChars = similarChars;
            this.restToSearch = restToSearch;
        }

        private void set(int lastPath, Node<E> reachedNode, int similarChars, StringBuilder restToSearch, StringBuilderPool stringBuilderPool) {
            this.lastPath = lastPath;
            this.reachedNode = reachedNode;
            this.similarChars = similarChars;
            stringBuilderPool.free(this.restToSearch);
            this.restToSearch = restToSearch;
        }
    }

    /**
     * This root node is parent of all nodes contained in this map. The root node always represents the empty
     * string (its isLeaf key will indicate if the empty string indeed is withing the included strings)
     */
    private Node<E> rootNode;

    private Set<String> nodeSet;

    private Set<E> valueSet;

    private Set<Entry<String, E>> entrySet;

    private int operationsToGc;

    private int opCounter;

    StringBuilderPool stringBuilderPool;

    private boolean useSets;


    /**
     * *********************** MEMORY OPTIMIZATIONS ***************************
     */

    // avoid creating these fields for each search, use only one that never is destroyed
    @SuppressWarnings({"FieldCanBeLocal"})
    private Node<E> searchInListTempNode = new Node<>(null, EMPTY_STRING);

    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchInListIndex;
    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchInListInsertIndex;
    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchInListTestIndex1;
    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchInListCharIndex;


    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchSimilarityCharIndex;
    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchSimilarityL1;
    @SuppressWarnings({"FieldCanBeLocal"})
    private int searchSimilarityL2;


    private PathResult<E> tempPathResult = new PathResult<>(0, null, 0, new StringBuilder(EMPTY_STRING));


    private static final String EMPTY_STRING = "";

    public ThesauriStringMap() {
        this(true, 0);
    }

    public ThesauriStringMap(boolean useSets, int operationsToGc) {
        rootNode = new Node<>(null, EMPTY_STRING);
        nodeSet = new HashSet<>();
        valueSet = new DuplicateHashSet<>();
        entrySet = new HashSet<>();
        this.useSets = useSets;
        this.operationsToGc = operationsToGc;
        opCounter = 0;
        stringBuilderPool = new StringBuilderPool();
    }

    private SearchResult searchInList(List<Node<E>> nodeList, StringBuilder key) {
        searchInListTempNode.key = key.toString();
        searchInListIndex = Collections.binarySearch(nodeList, searchInListTempNode);
        if (searchInListIndex >= 0) {
            // node completely found in this node list
            // two string requests for this result
            StringBuilder str1 = stringBuilderPool.request();
            StringBuilderPool.copy(key, str1);
            StringBuilder str2 = stringBuilderPool.request();
            StringBuilderPool.clear(str2);
            return new SearchResult(true, searchInListIndex, str1, str2);
        } else {
            // either partially or not found
            searchInListInsertIndex = -searchInListIndex - 1;

            // we must check the surrounding keys for common roots (it cannot be both, as no keys can share
            // any piece of root at any moment). The surrounding keys are at insertIndex - 1 and insertIndex
            searchInListTestIndex1 = searchInListInsertIndex - 1;

            if (searchInListTestIndex1 >= 0) {
                searchInListCharIndex = searchSimilarity(key, nodeList.get(searchInListTestIndex1).key);
                if (searchInListCharIndex > 0) {
                    // similarity found at this position
                    // two string requests and divide
                    StringBuilder str1 = stringBuilderPool.request();
                    StringBuilder str2 = stringBuilderPool.request();
                    StringBuilderPool.divide(key, str1, str2, searchInListCharIndex);
                    return new SearchResult(true, searchInListTestIndex1, str1, str2);
                }
            }
            if (searchInListInsertIndex < nodeList.size()) {
                searchInListCharIndex = searchSimilarity(key, nodeList.get(searchInListInsertIndex).key);
                if (searchInListCharIndex > 0) {
                    // similarity found at this position
                    // two string requests and divide
                    StringBuilder str1 = stringBuilderPool.request();
                    StringBuilder str2 = stringBuilderPool.request();
                    StringBuilderPool.divide(key, str1, str2, searchInListCharIndex);
                    return new SearchResult(true, searchInListInsertIndex, str1, str2);
                }
            }
            // no similarities found with any of the surrounding nodes
            // two string requests for this result
            StringBuilder str1 = stringBuilderPool.request();
            StringBuilderPool.clear(str1);
            StringBuilder str2 = stringBuilderPool.request();
            StringBuilderPool.copy(key, str2);
            return new SearchResult(false, searchInListInsertIndex, str1, str2);
        }
    }

    private int searchSimilarity(StringBuilder str1, String str2) {
        searchSimilarityCharIndex = 0;
        searchSimilarityL1 = str1.length();
        searchSimilarityL2 = str2.length();
        while (searchSimilarityCharIndex < searchSimilarityL1 && searchSimilarityCharIndex < searchSimilarityL2 && str1.charAt(searchSimilarityCharIndex) == str2.charAt(searchSimilarityCharIndex)) {
            searchSimilarityCharIndex++;
        }
        return searchSimilarityCharIndex;
    }

    private PathResult<E> searchPath(StringBuilder key) {
        return searchPathAux(rootNode, key);
    }

    private PathResult<E> searchPathAux(Node<E> currentNode, StringBuilder key) {
        if (key.length() == 0) {
            // the given node is the empty String, meaning that no further search must be done
            StringBuilder emptyStr = stringBuilderPool.request();
            StringBuilderPool.clear(emptyStr);
            tempPathResult.set(-1, currentNode, 0, emptyStr, stringBuilderPool);
            return tempPathResult;
        }
        SearchResult searchResult = searchInList(currentNode.children, key);
        if (!searchResult.found) {
            // no portion of the node was found in this node list. We add the obtained searchInListIndex to the path to
            // indicate where the node should be placed in this list
            StringBuilder str = stringBuilderPool.request();
            StringBuilderPool.copy(key, str);
            tempPathResult.set(searchResult.index, currentNode, searchResult.commonRoot.length(), str, stringBuilderPool);
            stringBuilderPool.free(searchResult.commonRoot);
            stringBuilderPool.free(searchResult.restToSearch);
            return tempPathResult;
        } else {
            // the node was totally or partially found in the list --> search one level deeper
            //if (currentNode.children.get(searchResult.index).key.equals(searchResult.commonRoot)) {
            if (StringBuilderPool.compareTo(searchResult.commonRoot, currentNode.children.get(searchResult.index).key) == 0) {
                // the found node is contained inside our key --> search one level deeper
                stringBuilderPool.free(searchResult.commonRoot);
                PathResult<E> res = searchPathAux(currentNode.children.get(searchResult.index), searchResult.restToSearch);
                stringBuilderPool.free(searchResult.restToSearch);
                return res;
            } else {
                // the found node is not contained in our key, but has some common root --> return the
                // corresponding PathResult
                tempPathResult.set(searchResult.index, currentNode.children.get(searchResult.index), searchResult.commonRoot.length(), searchResult.restToSearch, stringBuilderPool);
                stringBuilderPool.free(searchResult.commonRoot);
                return tempPathResult;
            }
        }
    }


    //@Override
    public int size() {
        int size = rootNode.childrenSize;
        if (rootNode.isLeaf) {
            size++;
        }
        return size;
    }

    //@Override
    public boolean isEmpty() {
        return !rootNode.isLeaf && rootNode.children.isEmpty();
    }

    //@Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            StringBuilder strKey = stringBuilderPool.request();
            StringBuilderPool.clear(strKey);
            strKey.append((String) key);
            PathResult pathResult = searchPath(strKey);
            stringBuilderPool.free(strKey);
            return pathResult.restToSearch.length() == 0 && pathResult.similarChars == 0 && pathResult.reachedNode.isLeaf;
        } else {
            return false;
        }
    }

    //@Override
    public boolean containsValue(Object value) {
        // performs a depth search on the tree, using the built-in iterator
        Iterator<String> it = iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (get(key).equals(value)) {
                return true;
            }
        }
        return false;
    }

    //@Override
    public E get(Object key) {
        if (key instanceof String) {
            StringBuilder strKey = stringBuilderPool.request();
            StringBuilderPool.clear(strKey);
            strKey.append((String) key);
            PathResult<E> pathResult = searchPath(strKey);
            if (pathResult.restToSearch.length() == 0 && pathResult.similarChars == 0 && pathResult.reachedNode.isLeaf) {
                return pathResult.reachedNode.value;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void increaseOpCounter() {
        if (operationsToGc > 0) {
            opCounter++;
            if (opCounter >= operationsToGc) {
                opCounter = 0;
                System.gc();
            }
        }
    }

    //@Override
    public E put(String key, E value) {
        StringBuilder keyBld = stringBuilderPool.request();
        StringBuilderPool.clear(keyBld);
        keyBld.append(key);
        increaseOpCounter();
        if (key.length() == 0) {
            E oldValue = rootNode.isLeaf ? rootNode.value : null;
            rootNode.isLeaf = true;
            rootNode.value = value;
            addToSets(EMPTY_STRING, value, oldValue);
            return oldValue;
        } else {
            PathResult<E> pathResult = searchPath(keyBld);
            // at this point we can have three different situations:
            // - Case 1: the root of our key was totally found, and some remaining part of the key must be added
            // ("aaa" + "aaabb"). We will see a value in restToSearch and no similar chars. The path points to
            // where that rest must be put. Here we must simply add the restToSearch where the path indicates
            //
            // - Case 2: our key was totally found ("aaa" + "aaa"). We see no restToSearch value and no similar chars.
            // The path points to the node that is equal to our key. We should just modify the existing similar node
            //
            // - Case 3: our key is contained inside some existing node ("aaa" + "aa"). We se no restToSearch and the
            // path points to the node which contains our key. Similar chars indicates how many of its chars are our
            // key. We must insert a new node with the common part right before the reached node, and substract from
            // this that common part, linking them by a parent-child relation
            //
            // - Case 4: There is a non-null intersection between a node and our key ("aaa" + "abb"). The path
            // points to the node with the intersection, restToSearch contains the part of our key not in common,
            // and similarChars indicates the maxSize of the intersection. A new node with the common part must be
            // created (but it will not be leaf). The reached node and the rest to search will hang from this node
            // (order must be calculated)
            //
            if (pathResult.restToSearch.length() > 0 && pathResult.similarChars == 0) {
                // case 1. Tested
                pathResult.reachedNode.children.add(pathResult.lastPath, new Node<>(pathResult.reachedNode, pathResult.restToSearch.toString(), value));
                modifySize(pathResult.reachedNode, 1);
                addToSets(key, value);
                stringBuilderPool.free(keyBld);
                return null;
            } else if (pathResult.restToSearch.length() == 0 && pathResult.similarChars == 0) {
                // case 2. Tested
                E oldValue = pathResult.reachedNode.isLeaf ? pathResult.reachedNode.value : null;
                if (!pathResult.reachedNode.isLeaf) {
                    // if this node was not a leaf, increase the children count for its parent
                    modifySize(pathResult.reachedNode.parentNode, 1);
                }
                pathResult.reachedNode.value = value;
                pathResult.reachedNode.isLeaf = true;
                addToSets(key, value, oldValue);
                stringBuilderPool.free(keyBld);
                return oldValue;
            } else if (pathResult.restToSearch.length() == 0 && pathResult.similarChars > 0) {
                // case 3. Tested
                divideNode(pathResult.reachedNode, pathResult.lastPath, pathResult.similarChars, key, true, value);
                stringBuilderPool.free(keyBld);
                return null;
            } else {
                // case 4. Tested
                Node<E> newMiddleNode = divideNode(pathResult.reachedNode, pathResult.lastPath, pathResult.similarChars, key, false, value);
                Node<E> newNode = new Node<>(newMiddleNode, pathResult.restToSearch.toString(), value);
                if (newNode.key.compareTo(pathResult.reachedNode.key) < 0) {
                    newMiddleNode.children.add(0, newNode);
                } else {
                    newMiddleNode.children.add(1, newNode);
                }
                modifySize(newMiddleNode, 1);
                addToSets(key, value);
                stringBuilderPool.free(keyBld);
                return null;
            }
        }
    }

    private void addToSets(String key, E value) {
        if (useSets) {
            addToSets(key, value, null);
        }
    }

    private void addToSets(String key, E value, E oldValue) {
        if (useSets) {
            nodeSet.add(key);
            valueSet.add(value);
            if (oldValue != null) {
                valueSet.remove(oldValue);
            }
            entrySet.add(new ThesauriStringMapEntry<>(this, key));
        }
    }

    private void removeFromSets(String key, E value) {
        if (useSets) {
            nodeSet.remove(key);
            if (value != null) {
                valueSet.remove(value);
            }
            entrySet.remove(new ThesauriStringMapEntry<>(this, key));
        }
    }


    private Node<E> divideNode(Node<E> node, int nodePositionInParent, int charsForNewNode, String fullKey, boolean newNodeIsLeaf, E value) {
        Node<E> newNode;
        if (newNodeIsLeaf) {
            newNode = new Node<>(node.parentNode, node.key.substring(0, charsForNewNode), value);
            modifySize(node.parentNode, 1);
            addToSets(fullKey, value);
        } else {
            newNode = new Node<>(node.parentNode, node.key.substring(0, charsForNewNode));
        }
        newNode.parentNode.children.set(nodePositionInParent, newNode);
        node.key = node.key.substring(charsForNewNode);
        node.parentNode = newNode;
        newNode.children.add(node);
        newNode.childrenSize = node.childrenSize;
        if (node.isLeaf) {
            newNode.childrenSize++;
        }
        return newNode;
    }

    private static <E> void modifySize(Node<E> node, int value) {
        node.childrenSize += value;
        if (node.parentNode != null) {
            modifySize(node.parentNode, value);
        }
    }

    @Override
    public E remove(Object key) {
        increaseOpCounter();
        if (key instanceof String) {
            StringBuilder strKey = stringBuilderPool.request();
            StringBuilderPool.clear(strKey);
            strKey.append((String) key);
            PathResult<E> pathResult = searchPath(strKey);
            if (pathResult.restToSearch.length() == 0 && pathResult.reachedNode.isLeaf) {
                // the node was found --> remove it
                // there are two specific cases:
                //
                // - Case 1: there are more than 1 children hanging from it, the node must stay there (we simply
                // put it as not leaf)
                //
                // - Case 2: there are 0 or 1 children, remove it as it is not necessary.
                //
                // NOTE: the root node is never removed, whatever amount of children it has, therefor it is treated
                // with case 1
                //
                if (pathResult.reachedNode.children.size() > 1 || pathResult.reachedNode.parentNode == null) {
                    // case 1. Tested
                    E oldValue = pathResult.reachedNode.isLeaf ? pathResult.reachedNode.value : null;
                    if (pathResult.reachedNode.isLeaf) {
                        modifySize(pathResult.reachedNode.parentNode, -1);
                        removeFromSets((String) key, oldValue);
                    }
                    pathResult.reachedNode.isLeaf = false;
                    return oldValue;
                } else {
                    // case 2
                    // first retrieve old value and modify parent's children maxSize if needed
                    E oldValue = pathResult.reachedNode.isLeaf ? pathResult.reachedNode.value : null;
                    if (pathResult.reachedNode.isLeaf) {
                        modifySize(pathResult.reachedNode.parentNode, -1);
                        removeFromSets((String) key, oldValue);
                    }
                    // the key at this node must be merged with all its children keys, and its children must now be
                    // children of the parent node
                    String keyToRemove = pathResult.reachedNode.key;
                    for (Node<E> child : pathResult.reachedNode.children) {
                        child.key = keyToRemove + child.key;
                    }
                    // now we insert these children in the appropriate position of the parent's children
                    Node<E> parent = pathResult.reachedNode.parentNode;
                    int insertIndex = pathResult.lastPath;
                    parent.children.addAll(insertIndex, pathResult.reachedNode.children);
                    return oldValue;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends E> m) {
        for (String key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public void clear() {
        increaseOpCounter();
        rootNode.children.clear();
        rootNode.childrenSize = 0;
        rootNode.isLeaf = false;
    }

    @Override
    public Set<String> keySet() {
        return nodeSet;
    }

    @Override
    public Collection<E> values() {
        return valueSet;
    }

    @Override
    public Set<Entry<String, E>> entrySet() {
        return entrySet;
    }

    public Iterator<String> iterator() {
        return new ThesauriStringMapIterator<>(this);
    }
}
