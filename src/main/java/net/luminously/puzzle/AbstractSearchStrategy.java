package net.luminously.puzzle;

import java.io.IOException;
import java.io.Writer;

import com.google.common.base.Throwables;

/**
 * Abstract class implementing several utility functions for use in search
 * strategy classes.
 */
public abstract class AbstractSearchStrategy implements SearchStrategy {
  private long start;
  public AbstractSearchStrategy() {
    start = System.currentTimeMillis();
  }
  
  /**
   * Returns the amount of seconds elapsed so far
   * @return Integer representation
   */
  protected int getElapsed() {
    return (int) ((System.currentTimeMillis() - start) / 1000);
  }
  
  /**
   * Utility function for writing results to file.
   * @param out Writer object for file
   * @param path String search path found
   */
  protected void write(Writer out, String path) {
    try {
      out.write(path + "\n" + getElapsed() + "\n");
    } catch (IOException e) {
      // Un-R/W-able file is a runtime exception
      Throwables.propagate(e);
    }
  }
}
