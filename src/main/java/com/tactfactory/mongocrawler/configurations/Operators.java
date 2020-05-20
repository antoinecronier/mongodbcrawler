package com.tactfactory.mongocrawler.configurations;

import java.util.ArrayList;
import java.util.List;

public enum Operators {
  EQUALS(0,"="),
  GREATER_THAN(1,">"),
  LOWER_THAN(2,"<"),
  GREATER_THAN_EQUALS(3,">="),
  LOWER_THAN_EQUALS(4, "<="),
  DIFFERENT(5,"!=");

  private final Integer ordinal;
  private final String display;

  private Operators(final Integer ordinal, final String display) {
    this.ordinal = ordinal;
    this.display = display;
  }

  public Integer getOrdinal() {
    return ordinal;
  }

  public String getDisplay() {
    return display;
  }

  public static List<String> getDisplayList(){
    final List<String> result = new ArrayList<>();

    for (Operators operator : Operators.values()) {
      result.add(operator.display);
    }

    return result;
  }

  public static List<String> getDisplayChoices(){
    final List<String> result = new ArrayList<>();

    int loop = 0;
    for (Operators operator : Operators.values()) {
      result.add(String.format("%s - %s", loop, operator.display));
      loop++;
    }

    return result;
  }
}
