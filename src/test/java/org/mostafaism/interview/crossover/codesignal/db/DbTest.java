package org.mostafaism.interview.crossover.codesignal.db;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

public class DbTest {

  @Test
  void level2() {
    final String[][] queries = new String[][] {new String[] {"SET_OR_INC", "C", "field1", "10"},
        new String[] {"TOP_N_KEYS", "5"}, new String[] {"SET_OR_INC", "A", "field1", "5"},
        new String[] {"DELETE", "A", "field1"}, new String[] {"SET_OR_INC", "B", "field1", "8"},
        new String[] {"SET_OR_INC", "B", "field1", "0"}, new String[] {"TOP_N_KEYS", "3"},
        new String[] {"DELETE", "B", "field2"}, new String[] {"SET_OR_INC", "B", "field2", "6"},
        new String[] {"SET_OR_INC", "A", "field1", "4"},
        new String[] {"SET_OR_INC", "A", "field1", "2"}, new String[] {"TOP_N_KEYS", "2"}};
    final String[] actual = QueriesRunner.runQueries(queries, new InMemoryDb());
    final String[] expected = new String[] {"10", "C(1)", "5", "true", "8", "8", "B(2), C(1)",
        "false", "6", "4", "6", "B(3), A(2)"};
    assertArrayEquals(expected, actual);
  }

  @Test
  void level3() {
    final String[][] queries = new String[][] {new String[] {"SET_OR_INC", "A", "B", "4"},
        new String[] {"UNLOCK", "A"}, new String[] {"LOCK", "user1", "A"},
        new String[] {"LOCK", "user2", "A"}, new String[] {"LOCK", "user3", "B"},
        new String[] {"UNLOCK", "B"}, new String[] {"SET_OR_INC", "A", "C", "5"},
        new String[] {"DELETE", "A", "B"},
        new String[] {"SET_OR_INC_BY_CALLER", "A", "B", "3", "user2"},
        new String[] {"DELETE_BY_CALLER", "A", "B", "user3"},
        new String[] {"SET_OR_INC_BY_CALLER", "A", "B", "5", "user1"}, new String[] {"UNLOCK", "A"},
        new String[] {"SET_OR_INC_BY_CALLER", "A", "B", "2", "user1"},
        new String[] {"SET_OR_INC_BY_CALLER", "A", "B", "1", "user2"},
        new String[] {"LOCK", "user3", "A"}, new String[] {"DELETE_BY_CALLER", "A", "B", "user2"},
        new String[] {"UNLOCK", "A"}};
    final String[] actual = QueriesRunner.runQueries(queries, new InMemoryDb());
    final String[] expected =
        new String[] {"4", "", "acquired", "wait", "invalid_request", "invalid_request", "",
            "false", "4", "false", "9", "released", "9", "10", "wait", "true", "released"};
    assertArrayEquals(expected, actual);
  }

}
