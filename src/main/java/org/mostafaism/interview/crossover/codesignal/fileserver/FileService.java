package org.mostafaism.interview.crossover.codesignal.fileserver;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface FileService {

  /**
   * 
   * @param fileName the name of the file to be created.
   * @param sizeInBytes the file size in bytes.
   * @throws RunTimeException if a file with the same name already exists on the server.
   */
  void upload(String fileName, int sizeInBytes);

  void uploadAt(String fileName, int sizeInBytes, Instant timestamp);

  void uploadAt(String fileName, int sizeInBytes, Instant timestamp, Duration ttl);

  /**
   * Returns an Optional of the file size if it exists, otherwise Optional.empty().
   * 
   * @param fileName the name of the file.
   * @return an Optional of the file size if it exists, otherwise Optional.empty().
   */
  Optional<Integer> get(String fileName);

  Optional<Integer> getAt(String fileName, Instant timestamp);

  /**
   * Copies sourceFile to a destination, or if the destination file already exists, it overwrites
   * the existing file.
   * 
   * @param sourceFile the file to be copied.
   * @param destination the destination to copy to.
   * @throws RuntimeException if sourceFile does not exist.
   */
  void copy(String sourceFile, String destination);

  void copyAt(String sourceFile, String destination, Instant timestamp);

  /**
   * Returns top 10 files starting with the provided prefix ordered by their size in descending
   * order, and in case of a tie by file name.
   * 
   * @param prefix file prefix to search for.
   * @return top 10 files starting with the provided prefix ordered by their size in descending
   *         order, and in case of a tie by file name.
   */
  List<String> searchByPrefix(String prefix);

  List<String> searchByPrefixAt(String prefix, Instant timestamp);

  /**
   * Rollsback the state of the FileService to the state at timestamp.
   * 
   * @param timestamp timestamp to rollback to.
   */
  void rollback(Instant timestamp);

  static interface FileServiceMemento {
    Instant timestamp();

    void restore();
  }
}
