package org.mostafaism.interview.crossover.codesignal.fileserver;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

record File(String name, int size, Instant createdAt, Duration ttl) implements Comparable<File> {

  @Override
  public int compareTo(File o) {
    final Comparator<File> sizeComparator = Comparator.comparingInt(File::size).reversed();
    final Comparator<File> nameComparator = Comparator.comparing(File::name);
    final Comparator<File> comparator = sizeComparator.thenComparing(nameComparator);
    return comparator.compare(this, o);
  }

  public boolean isExpired(Instant timestamp) {
    return timestamp.isBefore(createdAt)
        || Duration.between(createdAt, timestamp).compareTo(ttl) > 0;
  }
}
