package com.example.contacts;

import java.util.ArrayList;

class Node {
    Node[] childNodes = new Node[126];
    boolean eow = false;
    ArrayList<Integer> id = new ArrayList<>();

    public Node() {
        for(Node node : childNodes) {
            node = null;
        }
    }
}

class Trie {
    private static Node root;
    private static int totalNodes = 0;
    private static int totalWords = 0;

    public Trie() {
        root = new Node();
        root.eow = true;
    }

    public void insert(String word, int id) {
        insert(word, id, 0, root);
    }
    public int getTotalNodes() {
        return totalNodes;
    }
    public int getTotalWords() {
        return totalWords;
    }
    public ArrayList<Integer> getSortedIDs() {
        ArrayList<Integer> sortedIDs = new ArrayList<>();
        for(int i=0; i<root.childNodes.length; i++) {
            sortedIDs(root.childNodes[i], "", sortedIDs, i);
        }

        return sortedIDs;
    }


    private void insert(String word, int id, int i, Node root) {
        if(i == word.length()) {
            root.eow = true;
            root.id.add(id);
            totalWords++;
            return;
        }

        int indx = word.charAt(i);

        if(root.childNodes[indx] == null) { // add new node
            root.childNodes[indx] = new Node();
            totalNodes++;
        }

        insert(word, id, ++i, root.childNodes[indx]);
    }

    private ArrayList<Integer> sortedIDs(Node node, String word, ArrayList<Integer> IDs, int p) {
        if(node == null) {
            return null;
        }

        word += (char) p;
        if(node.eow) {
            for(int crrId : node.id) {
                IDs.add(crrId);
            }
        }

        for(int i=0; i<node.childNodes.length; i++) {
            sortedIDs(node.childNodes[i], word, IDs, i);
        }

        return IDs;
    }

}

