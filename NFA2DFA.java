import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;

public class NFA2DFA {

	public int numStates;
	public List<Character> sigmas;
	public List<Map<Character, List<Integer>>> transitions;
	public int initialState;
	public List<Integer> finalStates;
	public String strTest;
	private Set<Integer> acceptingStates;
	private Map<Integer, Map<Character, Set<Integer>>> transitionTable;

		/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	
	public static void main(String[] args) throws FileNotFoundException {
		String filename = args[0];
		Scanner input = new Scanner(new File(filename));
		NFA2DFA nfa = new NFA2DFA(input);
		NFA2DFA dfa = nfa.convertToDFA();

		try {
			FileWriter writer = new FileWriter("X.dfa");

			writer.write("   |Q|:   " + dfa.numStates + "\n");
			writer.write(" Sigma:");
			for (Character c : dfa.sigmas) {
				writer.write("   " + c);
			}
			writer.write("\n ------------------------------\n");
			for (int i = 0; i < dfa.numStates; i++) {
				writer.write("     " + i + ":");
				for (Character c : dfa.sigmas) {
					writer.write("   " + dfa.getTransitionState(c, i).get(0));
				}
				writer.write("\n");
			}
			writer.write(" ------------------------------\n");
			writer.write("Initial State: " + dfa.initialState + "\n");
			writer.write("Accepting State(s): ");
			if (dfa.finalStates.size() > 1) {
				for (int i = 0; i < dfa.finalStates.size() - 1; i++) {
					writer.write(dfa.finalStates.get(i) + ", ");
				}
			}
			writer.write(dfa.finalStates.get(0) + "\n");
			writer.write("\n-- Input strings for testing -----------\n\n");
			writer.write(dfa.strTest + "\n....\n");
			writer.close();
		} catch (IOException e) {
			System.err.println("Could not write to file");
			// e.printStackTrace();
		}

		System.out.println("NFA X.nfa to DFA X.dfa: \n");
		System.out.print(" Sigma:");
		for (Character c : dfa.sigmas) {
			System.out.print("   " + c);
		}
		System.out.println("\n ------------------------------");
		for (int i = 0; i < dfa.numStates; i++) {
			System.out.print("     " + i + ":");
			for (Character c : dfa.sigmas) {
				System.out.print("   " + dfa.getTransitionState(c, i).get(0));
			}
			System.out.println();
		}
		System.out.println(" ------------------------------");
		System.out.println(dfa.initialState + ":  Initial State ");
		if (dfa.finalStates.size() > 1) {
			for (int i = 0; i < dfa.finalStates.size() - 1; i++) {
				System.out.print(dfa.finalStates.get(i) + ", ");
				if (i == dfa.finalStates.size() - 2) {
					System.out.print(dfa.finalStates.get(i) + ":");
				}
			}
		}
		System.out.print("  Accepting State(s) \n");

		System.out.print("\nParsing result of strings attached in X.nfa:\n");

		// Strings for testing
		String str = dfa.strTest;
		List<String> charList = new ArrayList<>(); // Assuming strTest is already defined and contains the strings

		for (int i = 0; i < str.length(); i++) {
			charList.add(Character.toString(str.charAt(i)));
		}

		// Process and display the answers for each string
		int counter = 0;
		for (String str1 : charList) {
			boolean accepts = nfa.accepts(str1);
			System.out.print(accepts ? "Yes " : "No ");

			counter++;
			if (counter % 15 == 0) {
				System.out.println();
			}
			if (counter == 30) {
				System.out.print("\n");
				break; // Stop processing after 30 strings
			}
		}
	}

	public NFA2DFA() {
		this(0, new ArrayList<>(), new ArrayList<>(), -1, new ArrayList<>(), "");
	}

	public NFA2DFA(int states, List<Character> in, List<Map<Character, List<Integer>>> trans, int init, List<Integer> fin,
			String stringTest) {
		numStates = states;
		sigmas = in;
		transitions = trans;
		initialState = init;
		finalStates = fin;
		strTest = stringTest;

	}

	public NFA2DFA(Scanner input) {
		String line = input.nextLine();
		String[] parts = line.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid NFA file format: Missing colon delimiter in the first line.");
		}
		numStates = Integer.parseInt(parts[1].trim());

		line = input.nextLine();

		parts = line.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid NFA file format: Missing colon delimiter in the second line.");
		}
		sigmas = new ArrayList<>();
		String[] symbols = parts[1].trim().split("\\s+");
		for (String s : symbols) {
			sigmas.add(s.charAt(0));
		}
		sigmas.add(' ');

		while (!line.startsWith("----------------------------------------")) {
			line = input.nextLine();
		}

		transitions = new ArrayList<>();
		for (int i = 0; i < numStates; i++) {
			line = input.nextLine();
			Map<Character, List<Integer>> transitionState = new HashMap<>();
			parts = line.split(":");
			if (parts.length != 2) {
				throw new IllegalArgumentException(
						"Invalid NFA file format: Missing colon delimiter in the transition table.");
			}
			int state = Integer.parseInt(parts[0]);
			String[] stateTransitions = parts[1].trim().split("\\s+");
			if (stateTransitions.length != sigmas.size()) {
				throw new IllegalArgumentException(
						"Invalid NFA file format: Incorrect number of transition sets for state " + state);
			}
			for (int j = 0; j < sigmas.size(); j++) {
				List<Integer> nextState = new ArrayList<>();
				String[] nextStates = stateTransitions[j].replaceAll("[{}]", "").split(",");
				for (String nextStateStr : nextStates) {
					nextStateStr = nextStateStr.trim();
					if (!nextStateStr.isEmpty()) {
						nextState.add(Integer.parseInt(nextStateStr));
					}
				}
				transitionState.put(sigmas.get(j), nextState);
			}
			List<Integer> lambdaTransitions = new ArrayList<>();
			String[] lambdaStates = stateTransitions[stateTransitions.length - 1].replaceAll("[{}]", "").split(",");
			for (String lambdaStateStr : lambdaStates) {
				lambdaTransitions.add(Integer.parseInt(lambdaStateStr.trim()));
			}
			transitionState.put(' ', lambdaTransitions);
			transitions.add(transitionState);
		}

		line = input.nextLine();
		line = input.nextLine();

		parts = line.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException(
					"Invalid NFA file format: Missing colon delimiter in the line specifying the initial state.");
		}
		initialState = Integer.parseInt(parts[1].trim());

		line = input.nextLine();
		parts = line.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException(
					"Invalid NFA file format: Missing colon delimiter in the line specifying the accepting state(s).");
		}
		finalStates = new ArrayList<>();
		String[] finalStatesStr = parts[1].trim().split(",");
		for (String state : finalStatesStr) {
			finalStates.add(Integer.parseInt(state.trim()));
		}

		line = input.nextLine();

		strTest = input.nextLine();

	}

	public String parseTransition(String line, List<Integer> state) {
		line = line.substring(line.indexOf("{") + 1, line.length());

		if (!line.startsWith("}")) {
			while (line.indexOf(",") < line.indexOf("}") && line.indexOf(",") > 0) {
				if (!line.startsWith("λ")) {
					state.add(Integer.parseInt(line.substring(0, line.indexOf(","))));
				}
				line = line.substring(line.indexOf(",") + 1, line.length());
			}

			if (!line.startsWith("}")) {
				if (!line.startsWith("λ")) {
					state.add(Integer.parseInt(line.substring(0, line.indexOf("}"))));
				}
			}
		}

		return line.substring(line.indexOf("}") + 1, line.length());
	}

	public boolean evaluate(String str) throws Exception {
		int state = 0;
		str += "$";
		char ch = str.charAt(0);
	
		while (ch != '$') {
			if (sigmas.contains(ch)) {
				List<Integer> stateTransitions = transitions.get(state).get(ch);
				if (stateTransitions.size() > 0) {
					state = stateTransitions.get(0);
				} else {
					state = -1;
				}
				if (state == -1 && !finalStates.contains(state)) {
					return false;
				}
				str = str.substring(1);
				ch = str.charAt(0);
			} else {
				return false;
			}
		}
	
		return finalStates.contains(state);
	}
	

	public NFA2DFA convertToDFA() {
		NFA2DFA dfa = new NFA2DFA();
		dfa.initialState = this.initialState;
		List<List<Integer>> newStates = new ArrayList<>();
		List<Integer> lambdaClosure = new ArrayList<>();
		calculateLambdaClosure(0, lambdaClosure);
		newStates.add(lambdaClosure);
	
		List<Character> dfaSigmas = new ArrayList<>(this.sigmas);
		// Remove lambda from DFA
		dfaSigmas.remove(Character.valueOf(' '));
	
		List<Integer> current = new ArrayList<>(lambdaClosure);
		int totalState = 0, finalState = 0;
	
		while (current != null) {
			Map<Character, List<Integer>> dfaTransition = new HashMap<>();
	
			for (Character input : dfaSigmas) {
				List<Integer> tempStates = new ArrayList<>();
				for (Integer currentState : current) {
					for (Integer transitionState : this.transitions.get(currentState).get(input)) {
						calculateLambdaClosure(transitionState, tempStates);
					}
				}
	
				List<Integer> dfaState = new ArrayList<>();
				Collections.sort(tempStates);
				if (!newStates.contains(tempStates)) {
					newStates.add(tempStates);
					dfaState.add(++totalState);
				} else {
					dfaState.add(newStates.indexOf(tempStates));
				}
	
				dfaTransition.put(input, dfaState);
			}
	
			dfa.numStates++;
			dfa.transitions.add(dfaTransition);
	
			for (int state : finalStates) {
				if (current.contains(state)) {
					if (!dfa.finalStates.contains(finalState)) {
						dfa.finalStates.add(finalState);
						break;
					}
				}
			}
	
			if (++finalState >= newStates.size()) {
				current = null;
			} else {
				current = newStates.get(finalState);
			}
		}
	
		dfa.sigmas = dfaSigmas;
		dfa.strTest = strTest;
		return dfa;
	}
	
	public void minimize() {
		Set<Integer> reachableStates = getReachableStates();
		System.out.println(reachableStates.size());
	}

	public Set<Integer> getReachableStates() {
		Set<Integer> reachableStates = new HashSet<>();
		List<Integer> newStates = new ArrayList<>();
		Set<Integer> temp = new HashSet<>();
	
		reachableStates.add(0);
		newStates.add(0);
	
		while (!newStates.isEmpty()) {
			temp.clear();
	
			for (int i : newStates) {
				Map<Character, List<Integer>> newStateTransitions = this.transitions.get(i);
	
				for (char key : newStateTransitions.keySet()) {
					for (int j : newStateTransitions.get(key)) {
						temp.add(j);
					}
				}
			}
	
			newStates.clear();
			Object[] tempArray = temp.toArray();
			Object[] reachableArray = reachableStates.toArray();
	
			for (int i = 0; i < temp.size(); i++) {
				boolean isReachable = false;
	
				for (int j = 0; j < reachableStates.size(); j++) {
					if (tempArray[i].equals(reachableArray[j])) {
						isReachable = true;
						break;
					}
				}
	
				if (!isReachable) {
					newStates.add((Integer) tempArray[i]);
				}
			}
	
			if (!newStates.isEmpty()) {
				reachableStates.addAll(newStates);
			}
		}
	
		return reachableStates;
	}
	

	public boolean accepts(String input) {
		Set<Integer> currentStates = new HashSet<>();
		currentStates.add(0); // Add the initial state

		if (input == null) {
			return false; // Input is null, cannot be accepted
		}

		for (char c : input.toCharArray()) {
			Set<Integer> nextStates = new HashSet<>();

			if (transitionTable != null) {
				for (int state : currentStates) {
					if (transitionTable.containsKey(state) && transitionTable.get(state) != null
							&& transitionTable.get(state).containsKey(c)) {
						nextStates.addAll(transitionTable.get(state).get(c));
					}
				}
			}

			currentStates = nextStates;
		}

		if (acceptingStates != null) {
			for (int state : currentStates) {
				if (acceptingStates.contains(state)) {
					return true;
				}
			}
		}

		return false;
	}

	public void calculateLambdaClosure(int state, List<Integer> lambdaClosure) {
		if (lambdaClosure.contains(state)) {
			return;
		}
	
		lambdaClosure.add(state);
		Map<Character, List<Integer>> stateTransitions = transitions.get(state);
	
		if (stateTransitions.containsKey(' ')) {
			for (int i : stateTransitions.get(' ')) {
				calculateLambdaClosure(i, lambdaClosure);
			}
		}
	}
	

	public List<Integer> getTransitionState(char input, int state) {
		return this.transitions.get(state).get(input);
	}

}
