package org.mostafaism.interview.crossover.codesignal.db;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryDb implements Db {

  private static final String DEFAULT_USER = "DEFAULT_USER";

  private final Map<String, Record> records;
  private final Map<String, List<String>> locks;

  public InMemoryDb() {
    this.records = new HashMap<>();
    this.locks = new HashMap<>();
  }

  @Override
  public Optional<Integer> setOrInc(String key, String field, int value) {
    if (locked(key))
      return Optional.empty();
    return setOrIncByCaller(key, field, value, DEFAULT_USER);
  }

  @Override
  public Optional<Integer> setOrIncByCaller(String key, String field, int value, String callerId) {
    if (!allowedToModify(key, callerId))
      return records.containsKey(key) 
        ? Optional.of(get(key, field))
        : Optional.empty();

    final Record record = records.containsKey(key) 
      ? records.get(key) 
      : new Record(key);
    addRecord(record);
    final int currentValue = record.setOrInc(field, value);
    return Optional.of(currentValue);
  }

  @Override
  public boolean delete(String key, String field) {
    if (locked(key))
      return false;
    return deleteByCaller(key, field, DEFAULT_USER);
  }

  @Override
  public boolean deleteByCaller(String key, String field, String callerId) {
    if (!allowedToModify(key, callerId))
      return false;

    if (!records.containsKey(key))
      return false;

    final Record record = records.get(key);
    final boolean result = record.delete(field);
    if (record.isEmpty())
      removeRecord(key);
    return result;
  }

  @Override
  public List<Record> topNKeys(int n) {
    final List<Record> topNRecords = records.values()
      .stream()
      .sorted()
      .limit(n)
      .collect(Collectors.toList());
    return topNRecords;
  }

  @Override
  public LockResponse lock(String callerId, String key) {
    if (!records.containsKey(key))
      return LockResponse.INVALID_REQUEST;

    final List<String> queue = locked(key) 
      ? locks.get(key) 
      : new LinkedList<>();
    locks.put(key, queue);

    final LockResponse response = queue.isEmpty()
      ? LockResponse.ACQUIRED
      : queue.contains(callerId)
        ? LockResponse.ALREADY_IN_QUEUE
        : LockResponse.WAIT;

    if (!queue.contains(callerId))
      queue.add(callerId);

    return response;
  }

  @Override
  public UnLockResponse unlock(String key) {
    final UnLockResponse response = keyDeletedDuringLock(key)
      ? UnLockResponse.RELEASED
      : keyDoesNotExist(key)
        ? UnLockResponse.INVALID_REQUEST
        : locked(key)
          ? UnLockResponse.RELEASED
          : UnLockResponse.KEY_NOT_LOCKED;

    if (keyDeletedDuringLock(key))
      locks.remove(key);
    
    if (locked(key)) {
      var queue = locks.get(key);
      if (queue.size() > 1)
        locks.get(key).remove(0);
      else
        locks.remove(key);
    }
      
    return response;
  }

  private boolean allowedToModify(String key, String callerId) {
    return keyDoesNotExist(key) 
        || !locked(key)
        || lockedBy(key, callerId);
  }

  private int get(String key, String field) {
    return records.get(key).setOrInc(field, 0);
  }

  private void addRecord(Record record) {
    records.put(record.key(), record);
  }

  private void removeRecord(String key) {
    records.remove(key);
  }

  private boolean locked(String key) {
    return locks.containsKey(key);
  }

  private boolean keyDeletedDuringLock(String key) {
    return !records.containsKey(key) && locked(key);
  }

  private boolean keyDoesNotExist(String key) {
    return !records.containsKey(key) && !locked(key);
  }

  private boolean lockedBy(String key, String callerId) {
    return locks.get(key).get(0).equals(callerId);
  }

}
