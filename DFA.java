import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DFA {

    private int[][] transitionTable;
    private int[] acceptingStates;
    private int initialState;
    private int numStates;
    private int numSymbols;
    private char[] symbols;

    public DFA(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        // read number of states and symbols
        line = reader.readLine();
        numStates = Integer.parseInt(line.split(":")[1].trim());
        line = reader.readLine();
        symbols = line.split(":")[1].trim().replaceAll("\\s+", "").toCharArray();
        numSymbols = symbols.length;

        // initialize transition table
        transitionTable = new int[numStates][numSymbols];
        reader.readLine();
        // reader.readLine();

        // read transition table
        for (int i = 0; i < numStates; i++) {
            line = reader.readLine();
            String[] values = line.split(":")[1].trim().split("\\s+");
            for (int j = 0; j < numSymbols; j++) {
                transitionTable[i][j] = Integer.parseInt(values[j]);
            }
        }
        reader.readLine();

        // read initial state
        line = reader.readLine();
        initialState = Integer.parseInt(line.split(":")[1].trim());

        // read accepting states
        line = reader.readLine();
        acceptingStates = Arrays.stream(line.split(":")[1].trim().split(","))
                                .mapToInt(Integer::parseInt)
                                .toArray();

        reader.close();
    }

    public boolean accept(String input) {
        int currentState = initialState;
        for (int i = 0; i < input.length(); i++) {
            char symbol = input.charAt(i);
            int symbolIndex = Arrays.binarySearch(symbols, symbol);
            if (symbolIndex < 0) {
                return false; // invalid input symbol
            }
            currentState = transitionTable[currentState][symbolIndex];
        }
        return Arrays.binarySearch(acceptingStates, currentState) >= 0;
    }

    public static void main(String[] args) throws IOException {
      if(args.length < 1){
        System.err.println("Error.");
        System.exit(1);
      }
      String filename = args[0];
      try {
          DFA parser = new DFA(filename);
          BufferedReader reader = new BufferedReader(new FileReader(filename));
          String line = reader.readLine();
          while (line != null) {
              if (line.startsWith("-- Input strings for testing")) {
                  line = reader.readLine();
                  while (line != null) {
                      String input = line.trim();
                      boolean accepts = parser.accept(input);
                      System.out.println(input + ": " + (accepts ? "yes" : "no"));
                      line = reader.readLine();
                  }
              } else {
                  line = reader.readLine();
              }
          }
          reader.close();
      } catch (IOException e) {
          System.err.println(e);
      }
  }
}
