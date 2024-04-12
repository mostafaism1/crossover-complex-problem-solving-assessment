package org.mostafaism.interview.crossover.codesignal.fileserver;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryFileService implements FileService {

  private TreeMap<String, File> files;
  private final Stack<InMemoryFileServiceMemento> history;

  public InMemoryFileService() {
    this.files = new TreeMap<>();
    this.history = new Stack<>();
    history.push(snapshot(Instant.MIN));
  }

  @Override
  public void upload(String fileName, int sizeInBytes) {
    uploadAt(fileName, sizeInBytes, Instant.now());
  }

  @Override
  public void uploadAt(String fileName, int sizeInBytes, Instant timestamp) {
    uploadAt(fileName, sizeInBytes, timestamp, ChronoUnit.FOREVER.getDuration());
  }

  @Override
  public void uploadAt(String fileName, int sizeInBytes, Instant timestamp, Duration ttl) {
    if (files.containsKey(fileName))
      throw new IllegalStateException(String.format("File [%s] already exists.", fileName));

    final File file = new File(fileName, sizeInBytes, timestamp, ttl);
    files.put(fileName, file);
    history.push(snapshot(timestamp));
  }

  @Override
  public Optional<Integer> get(String fileName) {
    final Predicate<File> fileFilter = file -> true;
    return getWithFilter(fileName, fileFilter);
  }

  @Override
  public Optional<Integer> getAt(String fileName, Instant timestamp) {
    final Predicate<File> fileFilter = file -> !file.isExpired(timestamp);
    return getWithFilter(fileName, fileFilter);
  }

  private Optional<Integer> getWithFilter(String fileName, Predicate<File> fileFilter) {
    return Optional.ofNullable(files.get(fileName)).filter(fileFilter).map(File::size);
  }

  @Override
  public void copy(String sourceFile, String destination) {
    final Predicate<String> fileValidator = fileName -> files.containsKey(fileName);
    copyAt(sourceFile, destination, fileValidator, Instant.now());
  }

  @Override
  public void copyAt(String sourceFile, String destination, Instant timestamp) {
    final Predicate<String> fileValidator =
        fileName -> files.containsKey(fileName) && !files.get(fileName).isExpired(timestamp);
    copyAt(sourceFile, destination, fileValidator, timestamp);
  }

  private void copyAt(String sourceFile, String destination, Predicate<String> fileValidator,
      Instant timestamp) {
    if (!fileValidator.test(sourceFile))
      throw new IllegalStateException();

    final String[] fileParts = sourceFile.split("/");
    final String fileName = fileParts[fileParts.length - 1];
    final String destinationFile = String.format("%s/%s", destination, fileName);
    upload(destinationFile, files.get(sourceFile).size());
    history.push(snapshot(timestamp));
  }

  @Override
  public List<String> searchByPrefix(String prefix) {
    final Predicate<File> fileFilter = file -> true;
    return searchByPrefixAtWithFilter(prefix, fileFilter);
  }

  @Override
  public List<String> searchByPrefixAt(String prefix, Instant timestamp) {
    final Predicate<File> fileFilter = file -> !file.isExpired(timestamp);
    return searchByPrefixAtWithFilter(prefix, fileFilter);
  }

  private List<String> searchByPrefixAtWithFilter(String prefix, Predicate<File> fileFilter) {
    return files.values().stream().filter(fileFilter).sorted()
        .filter(f -> f.name().startsWith(prefix))
        .map(file -> String.format("%s(%d)", file.name(), file.size())).limit(10)
        .collect(Collectors.toList());
  }

  @Override
  public void rollback(Instant timestamp) {
    while (!history.isEmpty() && history.peek().timestamp.isAfter(timestamp))
      history.pop();

    final InMemoryFileServiceMemento memento = history.pop();
    memento.restore();
  }

  private InMemoryFileServiceMemento snapshot(Instant timestamp) {
    return new InMemoryFileServiceMemento(timestamp);
  }

  private class InMemoryFileServiceMemento implements FileServiceMemento {
    private final Instant timestamp;
    private final TreeMap<String, File> fileNameToFile;

    public InMemoryFileServiceMemento(Instant timestamp) {
      this.timestamp = timestamp;
      this.fileNameToFile = new TreeMap<>(InMemoryFileService.this.files);
    }

    @Override
    public Instant timestamp() {
      return timestamp;
    }

    @Override
    public void restore() {
      InMemoryFileService.this.files = fileNameToFile;
    }
  }
}
