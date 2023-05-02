import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.lang.Integer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class minimizeDFA{
    public static void main(String[] args) {
        
        if (args.length != 1)
        {
            System.err.println("Error --- USAGE: java minimizeDFA <filename>");
            return;
        }   

        String filename = args[0];
        File file = null;
        Scanner read = null;
        try{
            file = new File(filename);
            read = new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println("File could not be found");
            e.printStackTrace();
        }

        // Get the number of states in the dfa
        int numStates = getNumStates(read);
        //Gets the transitions
        String transitions = getTransitions(read);
        int numTransitions = transitions.length();
        //Map the states to their tansition states
        int[][] statesMatrix = fillStates(numStates, numTransitions, read);
        //Get the initial State
        int initialState = getInitialState(read);
        //Get the accepting states
        int[] acceptingStates = getAcceptingStates(read);
        //Manke the Minimization Table for the states
        boolean[][] minTable = fillMinTable(statesMatrix, acceptingStates, numStates, numTransitions);
        int[] equalStates = new int[numStates]; //passed by reference
        int numNewStates = getEquivalentStates(minTable, numStates, equalStates);
        int[][] newStateMatrix = newStateMatrix(statesMatrix, equalStates, numStates, numTransitions, numNewStates);
        int[] newAcceptStates = getNewAcceptingStates(acceptingStates, equalStates, numStates);
        int newInitialState = getNewInitialState(equalStates, initialState);

        String writefilename = "minDFA.txt";
        ArrayList<String> testStrings = getTestStrings(read);
        printDFA(filename, writefilename, testStrings, transitions, newStateMatrix, numStates, numNewStates, numTransitions, newAcceptStates, newInitialState);
        //Tests for part C
        printDFA(filename, "stuC.txt", testStrings, transitions, statesMatrix, numStates, numStates, numTransitions, acceptingStates, initialState);
        read.close();
    }

    public static int getNumStates(Scanner read)
    {
        String line = read.nextLine().trim();
        String[] tokens = line.split(":");
        int numStates = Integer.parseInt(tokens[1].trim());
        return numStates;
    }

    public static String getTransitions(Scanner read)
    {
        String line = read.nextLine().trim();
        String[] tokens = line.split(":");
        return tokens[1].replaceAll(" ", "");
    }

    public static int[][] fillStates(int numStates, int numTransitions, Scanner read)
    {
        read.nextLine();
        int[][] transTable = new int[numStates][numTransitions];
        for (int i = 0; i < numStates; i++)
        {
            String line = read.nextLine().trim();
            String[] tokens = line.split(":");
            String tranStates = tokens[1].replaceAll(" ", "");
            for (int j = 0; j < numTransitions; j++)
            {
                transTable[i][j] = Integer.parseInt(tranStates.charAt(j) + "");
            }
        }
        read.nextLine();
        return transTable;
    }

    public static int getInitialState(Scanner read)
    {
        String line = read.nextLine().trim();
        String[] tokens = line.split(":");
        String stateStr = tokens[1].replaceAll(" ", "");
        return Integer.parseInt(stateStr);
    }

    public static int[] getAcceptingStates(Scanner read)
    {
        String line = read.nextLine().trim();
        String[] strTokens = line.split(":");
        String[] stateTokens = strTokens[1].split(",");
        int[] acceptingStates = new int[stateTokens.length];
        for (int i = 0; i < stateTokens.length; i++)
        {
            String temp = stateTokens[i].replaceAll(" ", "");
            acceptingStates[i] = Integer.parseInt(temp);
        }
        return acceptingStates;
    }

    public static boolean[][] fillMinTable(int[][] statesMatrix, int[] acceptingStates, int numStates, int numTransitions)
    {
        boolean[][] minTable = new boolean[numStates][numStates];

       
        for (int i = 0; i < numStates; i++)
        {
            for (int j = i+1; j < numStates; j++)
            {
                if ((isAccepting(acceptingStates, i) && !isAccepting(acceptingStates, j)) || (!isAccepting(acceptingStates, i) && isAccepting(acceptingStates, j)))
                {
                    minTable[i][j] = true;
                }
            }
        }   

        for (int i = 0; i < numStates; i++)
        {
            for (int j = i+1; j < numStates; j++)
            {
                for (int k = 0; k < numTransitions; k++)
                {
                    int state1 = statesMatrix[i][k];
                    int state2 = statesMatrix[j][k];
                    if (minTable[state1][state2])
                    {
                        minTable[i][j] = true;
                        break;
                    }
                }
            }
        }
        return minTable;
    }

    public static boolean isAccepting(int[] acceptingStates, int state)
    {
        for (int i = 0; i < acceptingStates.length; i++)
        {
            if (acceptingStates[i] == state)
            {
                return true;
            }
        }
        return false;
    }
    
    public static int getEquivalentStates(boolean[][] minTable, int numStates, int[] equalStates)
    {
        Arrays.fill(equalStates, -1);
        int newNumStates = 0;
        for (int i = 0; i < numStates; i++)
        {
            if(equalStates[i] == -1)
            {
                equalStates[i] = newNumStates;
                for (int j = i+1; j < numStates; j++)
                {
                    if(!minTable[i][j])
                    {
                        equalStates[j] = newNumStates;
                    }
                }
                newNumStates++;
            }
        }
        return newNumStates;
    }


    public static int[][] newStateMatrix(int[][] statesMatrix, int[] equalStates, int numStates, int numTransitions, int numNewStates)
    {
        int[][] newStateMatrix = new int[numNewStates][numTransitions];
        for (int i = 0; i < numStates; i++)
        {
            int sourceState = equalStates[i];
            for (int j = 0; j < numTransitions; j++)
            {
                int newState = equalStates[statesMatrix[i][j]];
                newStateMatrix[sourceState][j] = newState;
            }
        }
        return newStateMatrix;
    }

    public static int[] getNewAcceptingStates(int[] acceptingStates, int[] equalStates, int numStates)
    {
        ArrayList<Integer> newAcceptStates = new ArrayList<Integer>();
        for (int i = 0; i < numStates; i++)
        {
            if(isAccepting(acceptingStates, i) && !newAcceptStates.contains(equalStates[i]))
            {
                newAcceptStates.add(equalStates[i]);
            }
        }

        int[] acceptingArr = new int[newAcceptStates.size()];
        for (int i = 0; i < acceptingArr.length; i++)
        {
            acceptingArr[i] = newAcceptStates.get(i);
        }
        return acceptingArr;
    }

    public static int getNewInitialState(int[] equalStates, int curInitialState)
    {
        return equalStates[curInitialState];
    }

    public static boolean parseDFAString(String input, int[][] stateMatrix, String symbols, int[] acceptingStates, int initialState)
    {
        int curState = initialState;
        for (int i = 0; i < input.length(); i++)
        {
            char terminal = input.charAt(i);
            int index = symbols.indexOf(terminal);
            if (index < 0)
            {
                return false;
            }
            curState = stateMatrix[curState][index];
        }

        for (int j = 0; j < acceptingStates.length; j++)
        {
            if (acceptingStates[j] == curState)
            {
                return true;
            }
        }
        return false;
    } 

    public static void printDFA(String readfilename, String writefilename, ArrayList<String> testStrings, String symbols, int[][] stateMatrix, int numOldStates, int numStates, int numTransitions, int[] acceptingStates, int initialState)
    {
        try{
            FileWriter writer = new FileWriter(writefilename);
            writer.write("Minimized DFA from " + readfilename + "\n");
            writer.write("Sigma:");
            for (char symbol : symbols.toCharArray())
            {
                writer.write("     " + symbol);
            }
            writer.write("\n------------------------------\n");
            for (int i = 0; i < numStates; i++)
            {
                writer.write("    " + i + ":");
                for (int j = 0; j < numTransitions; j++)
                {
                    writer.write("     " + stateMatrix[i][j]);
                }
                writer.write("\n");
            }
            writer.write("------------------------------\n");
            writer.write("Initial State: " + initialState + "\n");
            writer.write("Accepting State(s): ");
            for (int i = 0; i < acceptingStates.length; i++)
            {
                if (i == acceptingStates.length - 1)
                {
                    writer.write(acceptingStates[i] + "");
                }
                else
                {
                    writer.write(acceptingStates[i] + ",");
                }
            }
            writer.write("\n");
            int yesCount = 0;
            int noCount = 0;
            for (int i = 0; i < testStrings.size(); i++)
            {
                if (parseDFAString(testStrings.get(i), stateMatrix, symbols, acceptingStates, initialState))
                {
                    writer.write("Yes ");
                    yesCount++;
                }
                else
                {
                    writer.write("No ");
                    noCount++;
                }
                
                if (i == 14)
                {
                    writer.write("\n");
                }
            }
            writer.write("\nYes: " + yesCount + " No: " + noCount + "\n");
            writer.write("|Q| " + numOldStates + " -> " + numStates);
            writer.write("\n");
            writer.close();
        } catch (IOException e){
            System.err.println("Could not write to file");
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getTestStrings(Scanner read)
    {
        ArrayList<String> strings = new ArrayList<String>();
        read.nextLine();
        read.nextLine();
        read.nextLine();
        String line;
        while(read.hasNextLine())
        {
            line = read.nextLine();
            strings.add(line);
        }
        return strings;
    }
}


