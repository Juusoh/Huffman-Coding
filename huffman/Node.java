package huffman;

/**
 * Single node
 * @author Juuso Hyvönen
 *
 */
public class Node {
        private Node parent;
        private Node left;
        private Node right;
        private int key = -1;
        private int freq = -1;
        private boolean isLeaf = false;
        
        public Node() { 
        }
        
        public Node(int freq, int i) {  
                this.key = i;
                this.freq = freq;
        }
        
        public Node getParent() {
                return parent;
        }

        public void setParent(Node parent) {
                this.parent = parent;
        }

        public Node getLeft() {
                return left;
        }

        public void setLeft(Node left) {
                this.left = left;
        }

        public Node getRight() {
                return right;
        }

        public void setRight(Node right) {
                this.right = right;
        }

        public int getKey() {
                return key;
        }

        public void setKey(int key) {
                this.key = key;
        }

        public void setFreq(int freq) {
                this.freq = freq;
        }

        public int getFreq() {
                return freq;
        }
        
        public void setToLeaf() {
                isLeaf = true;
        }
        
        public boolean isLeaf() {
                return isLeaf;
        }
}
