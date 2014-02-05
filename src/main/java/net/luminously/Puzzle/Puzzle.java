package net.luminously.Puzzle;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Entrypoint for N-Puzzle project.
 */
public class Puzzle {
  private final static Runnable terminator = new Runnable() {
    // I'll be back
    @Override
    public void run() {
      try {
        Thread.sleep(1000 * 60 * 30);
      } catch (InterruptedException e) {
        Throwables.propagate(e);
      }
      System.exit(0);
    }
  };
  
  /**
   * Search for solutions given an initial board with specified search strategy.
   * @param board Initial configuration of the game board
   * @param searchStrategy SearchStrategy implementation to use
   * @param out Writer to write output to
   */
  public static void run(Board board, SearchStrategy searchStrategy, Writer out) {
    searchStrategy.search(board, out);
  }

	public static void main(String[] args) throws IOException {
		Preconditions.checkArgument(args.length == 2, "Usage: java Puzzle input.txt output.txt");

		Board board = Board.parse(args[0]);
		SearchStrategy searchStrategy = new AStarSearchStrategy();
		BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
		
		// Terminate forcefully after 30 minutes, as per instructions
		Executors.newSingleThreadExecutor().submit(terminator);
		
		run(board, searchStrategy, out);
		
		out.close();
	}

}
