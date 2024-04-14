package org.mostafaism.interview.crossover.codesignal.db;

import java.util.List;
import java.util.Optional;

public interface Db {
  Optional<Integer> setOrInc(String key, String field, int value);

  Optional<Integer> setOrIncByCaller(String key, String field, int value, String callerId);

  boolean delete(String key, String field);

  boolean deleteByCaller(String key, String field, String callerId);

  List<Record> topNKeys(int n);

  LockResponse lock(String callerId, String key);

  UnLockResponse unlock(String key);

  public static enum LockResponse {
    ACQUIRED("acquired"), WAIT("wait"), ALREADY_IN_QUEUE(""), INVALID_REQUEST("invalid_request");

    private final String message;

    private LockResponse(String message) {
      this.message = message;
    }

    public String message() {
      return message;
    }
  }

  public static enum UnLockResponse {
    RELEASED("released"), KEY_NOT_LOCKED(""), INVALID_REQUEST("invalid_request");

    private final String message;

    private UnLockResponse(String message) {
      this.message = message;
    }

    public String message() {
      return message;
    }
  }

}
