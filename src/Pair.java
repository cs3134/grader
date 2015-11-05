public class Pair<K extends Comparable<? super K>, V> implements Comparable<Pair<K, ?>> {
  public K key;
  public V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public int compareTo(Pair<K, ?> other) {
    return key.compareTo(other.key);
  }
}
