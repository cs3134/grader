import java.util.LinkedList;
import java.util.List;

public class Trie {

  private TrieNode root;

  public Trie() {
    root = new TrieNode('0', false);
  }

  public List<String> getStrings() {
    List<String> foundWords = new LinkedList<>();
    getStrings(root, foundWords, "");
    return foundWords;
  }

  private void getStrings(TrieNode node, List<String> list, String lettersSoFar) {
    // ignore top most node
    if (node.letter != '0') {
      lettersSoFar += node.letter;
    }

    if (node.endOfWord) {
      list.add(lettersSoFar);
    }

    for (TrieNode child : node.children) {
      if (child != null) {
        getStrings(child, list, lettersSoFar);
      }
    }
  }

  public boolean contains(String word) {
    TrieNode node = root;
    for (char character : word.toCharArray()) {
      if (node.children[character - 'a'] == null) {
        return false;
      } else {
        node = node.children[character - 'a'];
      }
    }
    return node.endOfWord;
  }

  public List<String> getStartsWith(String prefix) {
    TrieNode node = root;
    for (char character : prefix.toCharArray()) {
      if (node.children[character - 'a'] == null) {
        return new LinkedList<String>();
      } else {
        node = node.children[character - 'a'];
      }
    }
    List<String> list = new LinkedList<String>();

    if (node.endOfWord) {
      list.add(prefix);
    }

    for (TrieNode child : node.children) {
      if (child != null) {
        getStrings(child, list, prefix);
      }
    }
    return list;
  }

  public void addWord(String word) {
    TrieNode node = root;

    for (int i = 0; i < word.length(); i++) {
      char character = word.charAt(i);
      if (node.children[character - 'a'] == null) {
        if (i == word.length() - 1) {
          node.children[character - 'a'] = new TrieNode(character, true);
        } else {
          node.children[character - 'a'] = new TrieNode(character, false);
          node = node.children[character - 'a'];
        }
      } else {
        if (i == word.length() - 1) {
          node.children[character - 'a'].endOfWord = true;
          return;
        } else {
          node = node.children[character - 'a'];
        }
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    buildString(root, sb, 0);
    return sb.toString().trim();
  }

  public void buildString(TrieNode node, StringBuilder sb, int layer) {
    for (int i = 0; i < layer; i++) {
      sb.append(" ");
    }
    sb.append(node);
    sb.append("\n");
    for (TrieNode child : node.children) {
      if (child != null) {
        buildString(child, sb, layer + 1);
      }
    }
  }

  private class TrieNode {
    public char letter;
    public boolean endOfWord;
    public TrieNode[] children;

    public TrieNode(char letter, boolean endOfWord) {
      this.letter = letter;
      this.endOfWord = endOfWord;
      children = new TrieNode[26]; // number of alphabets in english
    }

    public String toString() {
      return endOfWord ? Character.toString(Character.toUpperCase(letter)) : Character.toString(letter);
    }
  }

  public static void main(String[] args) {
    Trie trie = new Trie();
    trie.addWord("hello");
    trie.addWord("help");
    System.out.println(trie);
    System.out.println(trie.getStartsWith("hell"));
  }
}
