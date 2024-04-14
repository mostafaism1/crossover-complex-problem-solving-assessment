package org.mostafaism.interview.crossover.codesignal.db;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Record implements Comparable<Record> {

  private final String key;
  private final Map<String, Integer> fields;
  private int modCount;

  public Record(String key) {
    this.key = key;
    this.fields = new HashMap<>();
    this.modCount = 0;
  }

  public String key() {
    return key;
  }

  public int modCount() {
    return modCount;
  }

  public int setOrInc(String field, int value) {
    modCount++;
    return fields.merge(field, value, Integer::sum);
  }

  public boolean delete(String field) {
    if (!fields.containsKey(field))
      return false;

    modCount++;
    return fields.remove(field) != null;
  }

  public boolean isEmpty() {
    return fields.isEmpty();
  }

  @Override
  public int compareTo(Record o) {
    final Comparator<Record> modCountComparator =
        Comparator.comparingInt((Record r) -> r.modCount).reversed();
    final Comparator<Record> keyComparator = Comparator.comparing((Record r) -> r.key);
    final Comparator<Record> comparator = modCountComparator.thenComparing(keyComparator);
    return comparator.compare(this, o);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass())
      return false;
    final Record other = (Record) obj;
    return key.equals(other.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

}
