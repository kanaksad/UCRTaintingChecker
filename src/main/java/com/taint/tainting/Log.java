package com.taint.tainting;

public class Log {

  private static final String fileName = "log.txt";

  public static void print(String message) {
    System.err.println(message);
  }
}
