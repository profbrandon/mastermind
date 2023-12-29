import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.util.Pair;

public class GameState {
    
    public final int slots;
    public final int colors;
    public final int maxRows;

    private final List<Row> rows;

    private Row solution;

    public GameState(final short[] solutionPegs) {
        this(4, 6, 8, solutionPegs);
    }

    public GameState(final int slots, final int colors, final int maxRows, final short[] solutionPegs) {
        this.slots  = Math.min(Math.max(slots, 2), 10);
        this.colors = Math.min(Math.max(colors, 2), Peg.PegColor.values().length);
        this.maxRows = maxRows;

        this.rows     = new ArrayList<>(this.maxRows);
        this.solution = new Row(solutionPegs, this.slots);

        for (int i = 0; i < this.maxRows; ++i) {
            this.rows.add(new Row(new short[this.slots], this.slots));
        }
    }

    public boolean setSolution(final short solution[]) {
        final Row row = new Row(solution, this.slots);
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
        final List<Byte> byteList = new ArrayList<>();
        
        byteList.addAll(shortToBytes((short) slots));
        byteList.addAll(shortToBytes((short) colors));
        byteList.addAll(shortToBytes((short) maxRows));
        
        byteList.addAll(this.solution.toByteList());

        for (final Row row : this.rows) {
            byteList.addAll(row.toByteList());
        }

        return byteList;
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

    private static List<Byte> shortToBytes(final short s) {
        final List<Byte> data = new ArrayList<>();

        data.add(Short.valueOf(s).byteValue());
        data.add(Short.valueOf((short) (s >> 8)).byteValue());

        return data;
    }

    private class Row {
        private final ArrayList<Optional<Peg>> pegs;
        
        public Row(final short pegShorts[], final int slots) {
            this.pegs = new ArrayList<>(slots);

            for (int i = 0; i < slots; ++i) {
                pegs.add(Peg.fromShort(pegShorts[i]));
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
            else {
                pegs.set(j, Optional.of(peg));
                return true;
            }
        }

        public boolean clearPeg(final int j) {
            if (j >= slots || j < 0) return false;
            else {
                pegs.set(j, Optional.empty());
                return true;
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
                final Short pegShort = pegs.get(i).map(p -> p.toShort()).orElse((short) 0);
                byteList.addAll(shortToBytes(pegShort));
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
