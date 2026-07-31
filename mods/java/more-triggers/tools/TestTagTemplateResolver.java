package gg.orbgenesis.moretriggers;

import java.util.Map;
import java.util.List;

public final class TestTagTemplateResolver {
  private TestTagTemplateResolver() {}

  public static void main(String[] args) {
    Map<String, String> tags =
        Map.of(
            "points", "42",
            "theme", "volcano",
            "special", "$5\\bonus");

    assertEquals(
        "Theme volcano: 42 points",
        TagTemplateResolver.resolve("Theme {theme}: {points} points", tags));
    assertEquals(
        "42 + 42", TagTemplateResolver.resolve("{points} + {points}", tags));
    assertEquals(
        "Unknown {missing}", TagTemplateResolver.resolve("Unknown {missing}", tags));
    assertEquals(
        "Reward $5\\bonus", TagTemplateResolver.resolve("Reward {special}", tags));
    assertEquals(
        "Source 10, nested 6",
        TagTemplateResolver.resolve(
            "Source {source}, nested {points}",
            List.of(Map.of("source", "10"), Map.of("points", "6"))));
    assertEquals(
        "Source wins: 10",
        TagTemplateResolver.resolve(
            "Source wins: {points}",
            List.of(Map.of("points", "10"), Map.of("points", "6"))));
    assertEquals("", TagTemplateResolver.resolve(null, tags));

    System.out.println("Tag template resolver tests passed.");
  }

  private static void assertEquals(String expected, String actual) {
    if (!expected.equals(actual)) {
      throw new AssertionError("Expected <" + expected + "> but got <" + actual + ">");
    }
  }
}
