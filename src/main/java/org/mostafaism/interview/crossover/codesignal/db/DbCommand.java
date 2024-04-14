package org.mostafaism.interview.crossover.codesignal.db;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mostafaism.interview.crossover.codesignal.db.Db.LockResponse;
import org.mostafaism.interview.crossover.codesignal.db.Db.UnLockResponse;

// A variation on the command pattern.
public enum DbCommand implements Function<String[], Function<Db, String>> {

  SET_OR_INC {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String key = query[1];
        final String field = query[2];
        final Integer value = Integer.valueOf(query[3]);
        final Optional<Integer> result = db.setOrInc(key, field, value);
        return result.map(i -> i.toString()).orElse("");
      };
    }
  },

  SET_OR_INC_BY_CALLER {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String key = query[1];
        final String field = query[2];
        final Integer value = Integer.valueOf(query[3]);
        final String callerId = query[4];
        final Optional<Integer> result = db.setOrIncByCaller(key, field, value, callerId);
        return result.map(i -> i.toString()).orElse("");
      };
    }
  },

  DELETE {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String key = query[1];
        final String field = query[2];
        final boolean result = db.delete(key, field);
        return Boolean.toString(result);
      };
    }
  },

  DELETE_BY_CALLER {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String key = query[1];
        final String field = query[2];
        final String callerId = query[3];
        final boolean result = db.deleteByCaller(key, field, callerId);
        return Boolean.toString(result);
      };
    }
  },

  TOP_N_KEYS {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final Integer n = Integer.valueOf(query[1]);
        final List<Record> result = db.topNKeys(n);
        return result.stream()
            .map(record -> String.format("%s(%d)", record.key(), record.modCount()))
            .collect(Collectors.joining(", "));
      };
    }
  },

  LOCK {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String callerId = query[1];
        final String key = query[2];
        final LockResponse result = db.lock(callerId, key);
        return result.message();
      };
    }
  },

  UNLOCK {
    @Override
    public Function<Db, String> apply(String[] query) {
      return (Db db) -> {
        final String key = query[1];
        final UnLockResponse result = db.unlock(key);
        return result.message();
      };
    }
  };

}
