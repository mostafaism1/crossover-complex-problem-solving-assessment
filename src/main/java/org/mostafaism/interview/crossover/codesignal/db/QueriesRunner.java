package org.mostafaism.interview.crossover.codesignal.db;

public class QueriesRunner {

  public static String[] runQueries(String[][] queries, Db db) {
    final String[] result = new String[queries.length];
    for (int i = 0; i < queries.length; i++) {
      result[i] = runQuery(queries[i], db);
    }
    return result;
  }

  private static String runQuery(String[] query, Db db) {
    final String commandName = query[0];
    final DbCommand command = DbCommand.valueOf(commandName);
    final String result = command.apply(query).apply(db);
    return result;
  }

}
