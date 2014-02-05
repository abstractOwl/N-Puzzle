package net.luminously.Puzzle;

import java.io.Writer;

/**
 * Interface representing methods to search for N-Puzzle solutions
 */
public interface SearchStrategy {
  /**
   * Returns a result of the search.
   * @param board Initial Board object
   * @param out File to write results to
   * @return String representing the path taken to arrive at solution
   */
  public void search(Board board, Writer out);
  
}
