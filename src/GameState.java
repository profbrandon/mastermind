import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.util.Pair;

public class GameState {
    private static final int DEFAULT_SLOTS  = 4;
    private static final int DEFAULT_COLORS = 6;
    private static final int DEFAULT_ROWS   = 8;
    
    public final int slots;
    public final int colors;
    public final int maxRows;

    private final List<Row> rows;

    private Row solution;

    public GameState() {
        this(GameState.randomSolution(DEFAULT_SLOTS, DEFAULT_COLORS));
    }

    public GameState(final byte[] solutionPegs) {
        this(DEFAULT_SLOTS, DEFAULT_COLORS, DEFAULT_ROWS, solutionPegs);
    }

    public GameState(final int slots, final int colors, final int maxRows, final byte[] solutionPegs) {
        this.slots  = Math.min(Math.max(slots, 2), 10);
        this.colors = Math.min(Math.max(colors, 2), Peg.PegColor.values().length);
        this.maxRows = maxRows;

        this.rows     = new ArrayList<>(this.maxRows);
        this.solution = new Row(solutionPegs, this.slots, false);

        for (int i = 0; i < this.maxRows; ++i) {
            this.rows.add(new Row(new byte[this.slots], this.slots, false));
        }

        this.rows.get(0).toggleEditable();
    }

    public boolean isRowFull(final int i) {
        return this.getRow(i).map(Row::isFull).orElse(false);
    }

    public boolean setSolution(final byte[] solution) {
        final Row row = new Row(solution, this.slots, false);
        if (row.isFull()) {
            this.solution = row;
            return true;
        } else {
            return false;
        }
    }

    public Optional<Peg> pegAt(final int i, final int j) {
        if (i >= rows.size() || i < 0) {
            return Optional.empty();
        } else {
            return rows.get(i).pegAt(j);
        }
    }

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

    public boolean setPeg(final int i, final int j, final Peg peg) {
        if (i < 0 || i >= maxRows) return false;
        else {
            return rows.get(i).setPeg(j, peg);
        }
    }

    public boolean clearPeg(final int i, final int j) {
        if (i < 0 || i >= maxRows) return false;
        else {
            return rows.get(i).clearPeg(j);
        }
    }

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

    @Override
    public String toString() {
        String str = "";

        for (final Row row : rows) {
            str += row.toString() + "\n";
        }

        return str;
    }

    private Optional<Row> getRow(final int i) {
        if (i < 0 || i >= maxRows) return Optional.empty();
        else {
            return Optional.of(rows.get(i));
        }
    }

    public static List<Byte> squeeze(final List<Byte> data) {
        if (data.size() % 2 != 0) return data;
        else {
            final List<Byte> newData = new ArrayList<>();

            for (int i = 0; i < data.size(); i += 2) {
                newData.add((byte) (data.get(i) | (data.get(i + 1) << 4)));
            }

            return newData;
        }
    }

    public static List<Byte> unsqueeze(final List<Byte> data) {
        final List<Byte> newData = new ArrayList<>();

        for (int i = 0; i < data.size(); ++i) {
            final byte temp = data.get(i);

            newData.add((byte) (temp & 0x0F));
            newData.add((byte) ((temp & 0xF0) >> 4));
        }

        return newData;
    }

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
            for (int j = 0; j < slots; ++j) {
                final Optional<Peg> peg = Peg.fromByte(pegData.remove(0));

                if (peg.isPresent()) {
                    state.setPeg(i, j, peg.get());
                }
            }
        }

        return state;
    }

    public static byte[] randomSolution(final int slots, final int colors) {
        final byte[] data = new byte[slots];

        for (int i = 0; i < slots; ++i) {
            data[i] = Peg.PegColor.randomPegColor(colors).toByte();
        }

        return data;
    }

    private class Row {
        private boolean isEditable;

        private final ArrayList<Optional<Peg>> pegs;
        
        public Row(final byte[] pegBytes, final int slots, final boolean isEditable) {
            this.pegs = new ArrayList<>(slots);
            this.isEditable = isEditable;

            for (int i = 0; i < slots; ++i) {
                pegs.add(Peg.fromByte(pegBytes[i]));
            }
        }

        public Optional<Peg> pegAt(final int j) {
            if (j >= slots || j < 0) {
                return Optional.empty();
            } else {
                return pegs.get(j);
            }
        }

        public boolean setPeg(final int j, final Peg peg) {
            if (j >= slots || j < 0) return false;
            else if (isEditable) {
                pegs.set(j, Optional.of(peg));
                return true;
            } else {
                return false;
            }
        }

        public boolean clearPeg(final int j) {
            if (j >= slots || j < 0) return false;
            else if (isEditable) {
                pegs.set(j, Optional.empty());
                return true;
            } else {
                return false;
            }
        }

        public boolean isFull() {
            for (int i = 0; i < slots; ++i) {
                if (pegs.get(i).isEmpty()) return false;
            }

            return true;
        }

        public int getRed(final Row row) {
            int counter = 0;
            
            for (int i = 0; i < slots; ++i) {
                final Optional<Peg> p1 = row.pegs.get(i);
                final Optional<Peg> p2 = this.pegs.get(i);

                if (p1.isPresent() && p2.isPresent()) {
                    if (p1.get().getColor() == p2.get().getColor()) {
                        ++counter;
                    }
                }
            }

            return counter;
        }

        public int getWhite(final Row row) {
            final int[] colors1 = this.getColorCount();
            final int[] colors2 = row.getColorCount();

            int counter = 0;

            for (int i = 0; i < Peg.PegColor.values().length; ++i) {
                counter += Math.min(colors1[i], colors2[i]);
            }

            return counter;
        }

        public void toggleEditable() {
            this.isEditable = !this.isEditable;
        }

        public boolean isEditable() {
            return this.isEditable;
        }

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

        public List<Byte> toByteList() {
            final List<Byte> byteList = new ArrayList<>(slots * 2);

            for (int i = 0; i < slots; ++i) {
                final Byte pegByte = pegs.get(i).map(p -> p.toByte()).orElse((byte) 0);
                byteList.add(pegByte);
            }

            return byteList;
        }
    
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
