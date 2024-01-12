import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.util.Pair;

/**
 * Class to represent the state of a game of {@link Mastermind}. Can be exported as a string
 * of bytes in the format:
 * 
 *            1 byte:  # of slots,
 *            1 byte:  # of colors,
 *            1 byte:  # of rows,
 *      (slots) bytes: solution pegs,
 * (slots*rows) bytes: guess pegs,
 * 
 * which are then compressed down from the total number of bytes (n) to (ceil(n/2)).
 */
public class GameState {
    private static final int DEFAULT_SLOTS  = 4;
    private static final int DEFAULT_COLORS = 6;
    private static final int DEFAULT_ROWS   = 8;
    
    public final int slots;
    public final int colors;
    public final int maxRows;

    private final List<Row> rows;

    private Row solution;

    /**
     * Default constructor that creates a game with 4, 6 colors, 8 rows and a random solution code.
     */
    public GameState() {
        this(GameState.randomSolution(DEFAULT_SLOTS, DEFAULT_COLORS));
    }

    /**
     * Constructor that creates a default game but with the specified solution.
     * 
     * @param solutionPegs the byte array representing the solution
     */
    public GameState(final byte[] solutionPegs) {
        this(DEFAULT_SLOTS, DEFAULT_COLORS, DEFAULT_ROWS, solutionPegs);
    }

    /**
     * Constructor that creates a game with the specified number of slots, colors, rows, and with
     * the specified solution. The correct bounds for the variables are:
     * 
     * 2 <= slots <= 10
     * 2 <= colors <= # of {@link Peg.PegColor}
     * 2 <= maxRows <= 20
     * 
     * The provided values are truncated to these ranges.
     * 
     * @param slots the number of slots
     * @param colors the number of colors
     * @param maxRows the number of rows
     * @param solutionPegs the byte array representing the solution
     */
    public GameState(final int slots, final int colors, final int maxRows, final byte[] solutionPegs) {
        this.slots   = Math.min(Math.max(slots, 2), 10);
        this.colors  = Math.min(Math.max(colors, 2), Peg.PegColor.values().length);
        this.maxRows = Math.min(Math.max(maxRows, 0), 20);

        this.rows     = new ArrayList<>(this.maxRows);
        this.solution = new Row(solutionPegs, this.slots, false);

        // Initialize all rows to be empty and uneditable
        for (int i = 0; i < this.maxRows; ++i) {
            this.rows.add(new Row(new byte[this.slots], this.slots, false));
        }

        // Only the first row should be initially editable
        this.rows.get(0).toggleEditable();
    }

    /**
     * @param i the row to select
     * @return whether the row is full of {@link Peg}s (returns false if the row is undefined)
     */
    public boolean isRowFull(final int i) {
        return this.getRow(i).map(Row::isFull).orElse(false);
    }

    /**
     * Sets the current solution of this {@link GameState} object to the specified solution.
     * 
     * @param solution the byte array representing the solution
     * @return whether the method was successful
     */
    public boolean setSolution(final byte[] solution) {
        final Row row = new Row(solution, this.slots, false);
        if (row.isFull()) {
            this.solution = row;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param i the ith row coordinate
     * @param j the jth column coordinate
     * @return the {@link Optional}<{@link Peg}> at row i and column j (if it exists)
     */
    public Optional<Peg> pegAt(final int i, final int j) {
        if (i >= rows.size() || i < 0) {
            return Optional.empty();
        } else {
            return rows.get(i).pegAt(j);
        }
    }

    /**
     * Computes the feedback for a given row. Feedback consists of two numbers, "red" and "white".
     * The "red" number records how many {@link Peg}s in the given row are the correct color and in the
     * correct position. The "white" number records how many {@link Peg}s are the correct color but in the
     * wrong position.
     * 
     * @param i the ith row coordinate
     * @return a pair of integers (r,w) representing the red and white responses
     */
    public Pair<Integer, Integer> testRow(final int i) {
        final Pair<Integer, Integer> fail = new Pair<Integer,Integer>(0, 0);
        final Optional<Row> temp = this.getRow(i);
        
        if (temp.isEmpty()) {
            return fail;
        } else {
            final Row rowToTest = temp.get();
            if (!rowToTest.isFull() || !solution.isFull()) {
                return fail;
            } else {
                final int reds = solution.getRed(rowToTest);
                final int whites = solution.getWhite(rowToTest);

                return new Pair<Integer,Integer>(reds, whites - reds);
            }
        }
    }

    /**
     * Sets the {@link Peg} at the specified location to be the provided {@link Peg}. 
     * 
     * @param i the ith row
     * @param j the jth column
     * @param peg the peg to put in this location
     * @return whether the set was successful
     */
    public boolean setPeg(final int i, final int j, final Peg peg) {
        if (i < 0 || i >= maxRows) return false;
        else {
            return rows.get(i).setPeg(j, peg);
        }
    }

    /**
     * Removes any {@link Peg} at the given location.
     * 
     * @param i the ith row
     * @param j the jth column
     * @return whether the removal was successful
     */
    public boolean clearPeg(final int i, final int j) {
        if (i < 0 || i >= maxRows) return false;
        else {
            return rows.get(i).clearPeg(j);
        }
    }

    /**
     * Interprets this {@link GameState} object as a list of bytes.
     * 
     * @return the {@link List}<{@link Byte}> representation of this object
     */
    public List<Byte> toByteList() {
        final List<Byte> data = new ArrayList<>();

        data.add((byte) slots);
        data.add((byte) colors);
        data.add((byte) maxRows);

        final List<Byte> pegData = new ArrayList<>();
        
        pegData.addAll(this.solution.toByteList());

        for (final Row row : this.rows) {
            pegData.addAll(row.toByteList());
        }

        data.addAll(GameState.squeeze(pegData));

        return data;
    }

    /**
     * Shifts which row is editable to the next row if possible.
     */
    public void nextRowIfPossible() {
        boolean found = false;

        for (final Row row : this.rows) {
            if (row.isEditable() && row.isFull()) {
                row.toggleEditable();
                found = true;
            } else if (found) {
                row.toggleEditable();
                return;
            }

        }
    }

    /**
     * Uses the characters specified for each type of {@link Peg} to create a string
     * representation of this object. Uses "-" characters for empty slots.
     * 
     * @return a {@link String} representation of this object
     */
    @Override
    public String toString() {
        String str = "";

        for (final Row row : rows) {
            str += row.toString() + "\n";
        }

        return str;
    }

    /**
     * Retrieves the row at the given location (or an empty {@link Optional} if the row
     * does not exist).
     * 
     * @param i the ith row
     * @return the {@link Optional}<{@link Row}>
     */
    private Optional<Row> getRow(final int i) {
        if (i < 0 || i >= maxRows) return Optional.empty();
        else {
            return Optional.of(rows.get(i));
        }
    }

    /**
     * Assumes the given list of bytes consists only of bytes where the top four bits
     * are zeros and squeezes the {@link List} of {@link Byte}s such that for two
     * consecutive bytes, the second byte is shifted and put into the upper four bits
     * of the last byte. This reduces the data by a factor of 2 (after an additional
     * byte is added to make an even number of input bytes).
     * 
     * @param data the data to squeeze
     * @return the data shrunk by a factor of n -> ceil(n/2)
     */
    public static List<Byte> squeeze(final List<Byte> data) {
        if (data.size() % 2 != 0) {
            data.add((byte) 0);
        }

        final List<Byte> newData = new ArrayList<>();

        for (int i = 0; i < data.size(); i += 2) {
            newData.add((byte) ((0x0F & data.get(i)) | (0xF0 & (data.get(i + 1) << 4))));
        }

        return newData;
    }

    /**
     * Performs the inverse operation to the {@link GameState#squeeze(List)} method.
     * 
     * @param data the data to unsqueeze
     * @return the data expanded by a factor of k -> 2 * k (optionally - 1)
     */
    public static List<Byte> unsqueeze(final List<Byte> data) {
        final List<Byte> newData = new ArrayList<>();

        for (int i = 0; i < data.size(); ++i) {
            final byte temp = data.get(i);

            newData.add((byte) (temp & 0x0F));
            newData.add((byte) ((temp >> 4) & 0x0F));
        }

        return newData;
    }

    /**
     * Takes a {@link List} of {@link Byte}s and builds a GameState object from the given
     * data. This method assumes that the data follows the format given in {@link GameState}.
     * 
     * @param data the data to build a {@link GameState} object from
     * @return the {@link GameState} object
     */
    public static GameState fromByteList(final List<Byte> data) {
        final int slots   = (int) data.remove(0);
        final int colors  = (int) data.remove(0);
        final int maxRows = (int) data.remove(0);

        final List<Byte> pegData = GameState.unsqueeze(data);

        final byte[] solution = new byte[slots];

        for (int i = 0; i < slots; ++i) {
            solution[i] = pegData.remove(0);
        }

        final GameState state = new GameState(slots, colors, maxRows, solution);
        
        for (int i = 0; i < maxRows; ++i) {
            state.getRow(i).ifPresent(row -> row.setEditable(true));

            for (int j = 0; j < slots; ++j) {
                final Optional<Peg> peg = Peg.fromByte(pegData.remove(0));

                if (peg.isPresent()) {
                    if (!state.setPeg(i, j, peg.get())) {
                        System.out.println("Failed to set peg at row " + i + " column " + j);
                    }
                }
            }
        }

        // Prepare which rows are editable
        boolean firstIncomplete = false;

        for (int i = 0; i < maxRows; ++i) {
            final Optional<Row> row = state.getRow(i);
            
            if (row.isPresent()) {
                row.get().setEditable(false);

                if (!firstIncomplete && !row.get().isFull()) {
                    firstIncomplete = true;
                    row.get().setEditable(true);
                }
            }
        }

        return state;
    }

    /**
     * Builds a byte array containing a random set of solution {@link Pegs} (as bytes).
     * 
     * @param slots the number of slots
     * @param colors the number of colors
     * @return the random solution
     */
    public static byte[] randomSolution(final int slots, final int colors) {
        final byte[] data = new byte[slots];

        for (int i = 0; i < slots; ++i) {
            data[i] = Peg.PegColor.randomPegColor(colors).toByte();
        }

        return data;
    }

    /**
     * Private class for representing a row of {@link Pegs}.
     */
    private class Row {
        private boolean isEditable;

        private final ArrayList<Optional<Peg>> pegs;
        
        /**
         * Constructs a {@link Row} from the given byte array of pegs with the given number of slots
         * and sets the editability.
         * 
         * @param pegBytes the byte array to derive peg information from
         * @param slots the number of slots present in the row
         * @param isEditable whether the row should be editable via a call to {@link Row#setPeg(int, Peg)} or
         *        {@link Row#clearPeg(int)}
         */
        public Row(final byte[] pegBytes, final int slots, final boolean isEditable) {
            this.pegs = new ArrayList<>(slots);
            this.isEditable = isEditable;

            for (int i = 0; i < slots; ++i) {
                pegs.add(Peg.fromByte(pegBytes[i]));
            }
        }

        /**
         * Retrieves the {@link Peg} at the given column coordinate. Returns an empty optional if the
         * coordinate is invalid.
         * 
         * @param j the jth column
         * @return the {@link Optional}<{@link Peg}>
         */
        public Optional<Peg> pegAt(final int j) {
            if (j >= slots || j < 0) {
                return Optional.empty();
            } else {
                return pegs.get(j);
            }
        }

        /**
         * Sets the {@link Peg} at the given column coordinate.
         * 
         * @param j the jth column
         * @param peg the {@link Peg} to place
         * @return whether the placement was successful
         */
        public boolean setPeg(final int j, final Peg peg) {
            if (j >= slots || j < 0) return false;
            else if (isEditable) {
                pegs.set(j, Optional.of(peg));
                return true;
            } else {
                return false;
            }
        }

        /**
         * Removes the {@link Peg} at the given column coordinate.
         * 
         * @param j the jth column
         * @return whether the removal was successful
         */
        public boolean clearPeg(final int j) {
            if (j >= slots || j < 0) return false;
            else if (isEditable) {
                pegs.set(j, Optional.empty());
                return true;
            } else {
                return false;
            }
        }

        /**
         * @return whether this {@link Row} is full
         */
        public boolean isFull() {
            for (int i = 0; i < slots; ++i) {
                if (pegs.get(i).isEmpty()) return false;
            }

            return true;
        }

        /**
         * Computes how many {@link Peg}s are the correct color and in the correct position
         * given the provided solution {@link Row}.
         * 
         * @param solution the solution {@link Row} to test against
         * @return the number of correct (red) {@link Peg}s
         */
        public int getRed(final Row solution) {
            int counter = 0;
            
            for (int i = 0; i < slots; ++i) {
                final Optional<Peg> p1 = solution.pegs.get(i);
                final Optional<Peg> p2 = this.pegs.get(i);

                if (p1.isPresent() && p2.isPresent()) {
                    if (p1.get().getColor() == p2.get().getColor()) {
                        ++counter;
                    }
                }
            }

            return counter;
        }

        /**
         * Computes how many {@link Peg}s are the correct color. This method does not care where the
         * pegs are, and so it also includes the "red" pegs.
         * 
         * @param solution the solution {@link Row} to test against 
         * @return how many {@link Peg}s are the correct color.
         */
        public int getWhite(final Row solution) {
            final int[] colors1 = this.getColorCount();
            final int[] colors2 = solution.getColorCount();

            int counter = 0;

            for (int i = 0; i < Peg.PegColor.values().length; ++i) {
                counter += Math.min(colors1[i], colors2[i]);
            }

            return counter;
        }

        /**
         * Toggles the editablility of this {@link Row}. (Determines whether valid calls to {@link Row#setPeg(int, Peg)} 
         * and {@link Row#clearPeg(int)} are successful.)
         */
        public void toggleEditable() {
            this.isEditable = !this.isEditable;
        }

        /**
         * Sets the editablility of this {@link Row}. (Determines whether valid calls to {@link Row#setPeg(int, Peg)} 
         * and {@link Row#clearPeg(int)} are successful.)
         * 
         * @param isEditable whether this {@link Row} should accept edits
         */
        public void setEditable(final boolean isEditable) {
            this.isEditable = isEditable;
        }

        /**
         * @return whether this row is currently editable
         */
        public boolean isEditable() {
            return this.isEditable;
        }

        /**
         * Computes an array of integers with a length equal to the number of {@link Peg.PegColor}s.
         * The array is then stocked with the number of {@link Peg}s of that {@link Peg.PegColor} present
         * in this row.
         * 
         * @return the integer array of color counts
         */
        private int[] getColorCount() {
            final int size = Peg.PegColor.values().length;

            final int data[] = new int[size];

            for (int i = 0; i < size; ++i) {
                data[i] = 0;
            }

            for (final Optional<Peg> peg : pegs) {
                peg.ifPresent(p -> {
                    ++data[p.getColor().ordinal()];
                });
            }

            return data;
        }

        /**
         * Interprets this {@link Row} as a list of bytes.
         * 
         * @return the {@link List}<{@link Byte}> representation of this {@link Row}
         */
        public List<Byte> toByteList() {
            final List<Byte> byteList = new ArrayList<>(slots);

            for (int i = 0; i < slots; ++i) {
                byteList.add(pegs.get(i).map(p -> p.toByte()).orElse((byte) 0));
            }

            return byteList;
        }
    
        /**
         * Uses the characters specified for each type of {@link Peg} to create a string
         * representation of this object. Uses "-" characters for empty slots.
         * 
         * @return a {@link String} representation of this object
         */
        @Override
        public String toString() {
            String str = "";

            for (final Optional<Peg> peg : pegs) {
                str += peg.isPresent() ? peg.get().toString() : "-";
            }

            return str;
        }
    }
}
