import java.util.List;
import java.util.stream.Collectors;

public class AvlMap<K extends Comparable<? super K>, V> implements Map<K, V> {

  private AvlTree<Pair<K, V>> data;

  public AvlMap() {
    data = new AvlTree<>();
  }

  @Override
  public void put(K key, V value) {
    data.insert(new Pair<K, V>(key, value));
  }

  @Override
  public V get(K key) {
    Pair<K, V> pair = data.get(new Pair<K, V>(key, null));
    return pair == null ? null : pair.value;
  }

  public List<K> keyList() {
    return data.toListInOrder().stream().map((Pair<K, V> pair) -> pair.key).collect(Collectors.toList());
  }
}
