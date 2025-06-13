import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SudokuSolver
{

    public static void main(String[] args) {
        SudokuSolver sudoku = new SudokuSolver();
        sudoku.parseTable();
        sudoku.solve();
        sudoku.printTable();
    }

    private SudokuSquare[][][][] table = new SudokuSquare[3][3][3][3];
    private int depth;

    public SudokuSolver() { depth = 0; }

    public SudokuSolver(SudokuSquare[][][][] table, int depth) {
        this.table = table;
        this.depth = depth;
    }

    private void parseTable() {
        try {
            File file = new File("SudokuTable.txt");
            Scanner scanner = new Scanner(file);
            Scanner lineScanner;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    lineScanner = new Scanner(scanner.nextLine());
                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < 3; l++) {
                            int num = lineScanner.nextInt();
                            if (num == 0) {
                                table[i][k][j][l] = new SudokuSquare();
                            }
                            else {
                                table[i][k][j][l] = new SudokuSquare(num);
                            }
                        }
                    }
                    lineScanner.close();
                }
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean solve() {
        while (true) {
            do {
                simpleElimination();
            }
            while (fillSquares());

            int x = 1;
            do {
                deductiveElimination(x++);
            }
            while (!fillSquares() && x != 9);

            if (x == 9) {
                break;
            }
        }
        if (contradiction()) {
            return false;
        }

        if (!solved()) {
            for (int n = 2; n <= 3 * 3; n++) {
                for (int i = 0; i < 3 * 3; i++) {
                    for (int j = 0; j < 3 * 3; j++) {
                        if (table[i / 3][j / 3][i % 3][j % 3].possible.size() == n) {
                            int temp = 1;
                            for (int choice : table[i / 3][j / 3][i % 3][j % 3].possible) {
                                System.out.println("Depth: " + depth + ", Choice: " + temp + " / " + n);
                                SudokuSolver solver = new SudokuSolver(copyTable(), depth + 1);
                                solver.table[i / 3][j / 3][i % 3][j % 3] = new SudokuSquare(choice);
                                if (solver.solve()) {
                                    table = solver.table;
                                    return true;
                                }
                                temp++;
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return solved();
    }

    private boolean solved() {
        for (int i = 0; i < 3 * 3; i++) {
            for (int j = 0; j < 3 * 3; j++) {
                if (table[i / 3][j / 3][i % 3][j % 3].num == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean contradiction() {
        for (int i = 0; i < 3 * 3; i++) {
            HashSet<Integer> rowSet = new HashSet<Integer>();
            HashSet<Integer> colSet = new HashSet<Integer>();
            HashSet<Integer> tableSet = new HashSet<Integer>();
            for (int j = 0; j < 3 * 3; j++) {
                if (table[i / 3][j / 3][i % 3][j % 3].possible.isEmpty()) {
                    return true;
                }
                if (table[i / 3][j / 3][i % 3][j % 3].num != 0 && !rowSet.add(table[i / 3][j / 3][i % 3][j % 3].num)) {
                    return true;
                }
                if (table[j / 3][i / 3][j % 3][i % 3].num != 0 && !colSet.add(table[j / 3][i / 3][j % 3][i % 3].num)) {
                    return true;
                }
                if (table[i / 3][i % 3][j / 3][j % 3].num != 0 && !tableSet.add(table[i / 3][i % 3][j / 3][j % 3].num)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean fillSquares() {
        boolean retVal = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        if (table[i][j][k][l].num == 0) {
                            if (table[i][j][k][l].possible.size() == 1) {
                                for (Integer num : table[i][j][k][l].possible) {
                                    table[i][j][k][l].num = num;
                                    break;
                                }
                                retVal = true;
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    private void simpleElimination() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        if (table[i][j][k][l].num == 0) {
                            for (int x = 0; x < 3; x++) {
                                for (int y = 0; y < 3; y++) {
                                    if (table[i][j][x][y].num != 0) {
                                        table[i][j][k][l].possible.remove(table[i][j][x][y].num);
                                    }
                                    if (table[i][x][k][y].num != 0) {
                                        table[i][j][k][l].possible.remove(table[i][x][k][y].num);
                                    }
                                    if (table[x][j][y][l].num != 0) {
                                        table[i][j][k][l].possible.remove(table[x][j][y][l].num);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void deductiveElimination(int val) {
        for (int i = 0; i < 3 * 3; i++) {
            deductiveEliminationRow(val, i);
            deductiveEliminationCol(val, i);
            deductiveEliminationTable(val, i);
        }
    }

    private void deductiveEliminationRow(int val, int i) {
        HashSet<Integer> remaining = new HashSet<>(List.of(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
        for (int j = 0; j < 3 * 3; j++) {
            if (table[i / 3][j / 3][i % 3][j % 3].num != 0) {
                remaining.remove(table[i / 3][j / 3][i % 3][j % 3].num);
            }
        }
        if (remaining.size() < val) {
            return;
        }
        LinkedList<HashSet<Integer>> permutations = getPermutations(val, new ArrayList<Integer>(remaining));
        for (HashSet<Integer> set : permutations) {
            HashSet<Integer> squares = new HashSet<Integer>();
            for (int j = 0; j < 3 * 3; j++) {
                if (table[i / 3][j / 3][i % 3][j % 3].num == 0 && isSubSet(set, table[i / 3][j / 3][i % 3][j % 3].possible)) {
                    squares.add(j);
                }
            }
            if (squares.size() >= val) {
                HashSet<Integer> values = new HashSet<Integer>();
                for (int j : squares) {
                    values.addAll(table[i / 3][j / 3][i % 3][j % 3].possible);
                }
                for (int j = 0; j < 3 * 3; j++) {
                    if (table[i / 3][j / 3][i % 3][j % 3].num == 0 && !squares.contains(j)) {
                        table[i / 3][j / 3][i % 3][j % 3].possible.removeAll(values);
                    }
                }
            }
        }
    }

    private void deductiveEliminationCol(int val, int i) {
        HashSet<Integer> remaining = new HashSet<Integer>(List.of(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
        for (int j = 0; j < 3 * 3; j++) {
            if (table[j / 3][i / 3][j % 3][i % 3].num != 0) {
                remaining.remove(table[j / 3][i / 3][j % 3][i % 3].num);
            }
        }
        if (remaining.size() < val) {
            return;
        }
        LinkedList<HashSet<Integer>> permutations = getPermutations(val, new ArrayList<Integer>(remaining));
        for (HashSet<Integer> set : permutations) {
            HashSet<Integer> squares = new HashSet<Integer>();
            for (int j = 0; j < 3 * 3; j++) {
                if (table[j / 3][i / 3][j % 3][i % 3].num == 0 && isSubSet(set, table[j / 3][i / 3][j % 3][i % 3].possible)) {
                    squares.add(j);
                }
            }
            if (squares.size() >= val) {
                HashSet<Integer> values = new HashSet<Integer>();
                for (int j : squares) {
                    values.addAll(table[j / 3][i / 3][j % 3][i % 3].possible);
                }
                for (int j = 0; j < 3 * 3; j++) {
                    if (table[j / 3][i / 3][j % 3][i % 3].num == 0 && !squares.contains(j)) {
                        table[j / 3][i / 3][j % 3][i % 3].possible.removeAll(values);
                    }
                }
            }
        }
    }

    private void deductiveEliminationTable(int val, int i) {
        HashSet<Integer> remaining = new HashSet<Integer>(List.of(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
        for (int j = 0; j < 3 * 3; j++) {
            if (table[i / 3][i % 3][j / 3][j % 3].num != 0) {
                remaining.remove(table[i / 3][i % 3][j / 3][j % 3].num);
            }
        }
        if (remaining.size() < val) {
            return;
        }
        LinkedList<HashSet<Integer>> permutations = getPermutations(val, new ArrayList<Integer>(remaining));
        for (HashSet<Integer> set : permutations) {
            HashSet<Integer> squares = new HashSet<Integer>();
            for (int j = 0; j < 3 * 3; j++) {
                if (table[i / 3][i % 3][j / 3][j % 3].num == 0 && isSubSet(set, table[i / 3][i % 3][j / 3][j % 3].possible)) {
                    squares.add(j);
                }
            }
            if (squares.size() >= val) {
                HashSet<Integer> values = new HashSet<Integer>();
                for (int j : squares) {
                    values.addAll(table[i / 3][i % 3][j / 3][j % 3].possible);
                }
                for (int j = 0; j < 3 * 3; j++) {
                    if (table[i / 3][i % 3][j / 3][j % 3].num == 0 && !squares.contains(j)) {
                        table[i / 3][i % 3][j / 3][j % 3].possible.removeAll(values);
                    }
                }
            }
        }
    }

    private static LinkedList<HashSet<Integer>> getPermutations(int val, ArrayList<Integer> list) {
        LinkedList<HashSet<Integer>> permutations = new LinkedList<HashSet<Integer>>();
        if (!(val > list.size())) {
            getPermutations(permutations, val, new HashSet<Integer>(), list);
        }
        return permutations;
    }

    private static void getPermutations(LinkedList<HashSet<Integer>> permutations, int val, HashSet<Integer> currPermutation, ArrayList<Integer> list) {
        if (val == 1) {
            for (int i : list) {
                HashSet<Integer> newPermutation = new HashSet<Integer>(currPermutation);
                newPermutation.add(i);
                permutations.add(newPermutation);
            }
            return;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            HashSet<Integer> newPermutation = new HashSet<Integer>(currPermutation);
            newPermutation.add(list.get(i));
            ArrayList<Integer> newList = new ArrayList<Integer>(list.subList(i + 1, list.size()));
            getPermutations(permutations, val - 1, newPermutation, newList);
        }
    }

    private static boolean isSubSet(HashSet<Integer> set, HashSet<Integer> possibleSubSet) {
        for (int i : possibleSubSet) {
            if (!set.contains(i)) {
                return false;
            }
        }
        return true;
    }

    private void printTable() {
        System.out.println();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        System.out.print(table[i][k][j][l].num + " ");
                    }
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    private void printPossibilities() {
        System.out.println();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        System.out.println(table[i][k][j][l]);
                    }
                }
            }
        }
        System.out.println();
    }

    private SudokuSquare[][][][] copyTable() {
        SudokuSquare[][][][] newTable = new SudokuSquare[3][3][3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        newTable[i][j][k][l] = new SudokuSquare();
                        newTable[i][j][k][l].num = table[i][j][k][l].num;
                        newTable[i][j][k][l].possible = new HashSet<Integer>(table[i][j][k][l].possible);
                    }
                }
            }
        }
        return newTable;
    }

    private class SudokuSquare {

        private int num;
        private HashSet<Integer> possible;

        public SudokuSquare() {
            num = 0;
            possible = new HashSet<Integer>(List.of(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
        }

        public SudokuSquare(int num) {
            this.num = num;
            possible = new HashSet<Integer>(List.of(new Integer[]{num}));
        }

        public String toString() {
            String string = "< ";
            for (int i : possible) {
                string += i + " ";
            }
            string += ">";
            return string;
        }

    }
}
