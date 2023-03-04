package com.csc.Mathematics;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import java.math.BigInteger;

import java.nio.ByteBuffer;


class Main extends Cuboid {
    public static void main(String[] args) {
        /*
         * Main Entry Point of the Program
        */
        UnSigned<Integer> length = new UnSigned<Integer>(81);
        UnSigned<Integer> width = new UnSigned<Integer>(7);
        // UnSigned<Integer> width = new UnSigned<Integer>(100);
        UnSigned<Integer> height = new UnSigned<Integer>(17);
        Cuboid cuboid = Cuboid.initialize(length, width, height);

        // int least = Main.findLeastNumberOfRequiredMoves(cuboid);
        // System.out.println(least);

        // int[] playerScores = Main.findPlayerScores(cuboid, 2);

        // <2, a, b> || if a mod 2 == 0 then R alternates startging 1, 0
        //                                   at <2, a, a> R = 0 
        //                                   Remaining become 0 
        // <2, a, b> || if a mod 2 != 0 then it behaves like a mod 2 == 0 until <2, a, a>
        //                                   Peak is what odd number a is. at <2, a, a>
        //                                   Remaining become 1 
        double[] RandI = Main.findPeaksIn2PlayerGame(3, 9);
        System.out.println(String.format("R is %f at %f",  RandI[0], RandI[1]));
    }

    /**
     * Plays a move
     *
     * @param  cuboid  the cuboid on which the game is played
     * 
     * @see            {@link Main.next}
    */
    public static void next(Cuboid cuboid) {
        cuboid.positions = cuboid.collect(cuboid.best());
        // cuboid.display();
    }

    /**
     * Finds the peaks of the Parabolic approximation of
     * 
     * 
     * @see            {@link Main.next}
     * @see            {@link Main.findPlayerScores}
     * @see            {@link Cuboid}
     */
    public static double[] findPeaksIn2PlayerGame(int x, int y) {
        Cuboid cuboid = new Cuboid();
        cuboid.width = (UnSigned<Integer>) UnSigned.of(x);
        cuboid.height = (UnSigned<Integer>) UnSigned.of(y);

        int currentMinimum = Math.min(x, y);

        // for (int i = Math.max(x, y); ; ++i) {
        for (int i = 1; ; ++i) {
            cuboid.length = (UnSigned<Integer>) UnSigned.of(i);
            cuboid = cuboid.initialize(cuboid.length, cuboid.width, cuboid.height);
            
            int [] playerScores = Main.findPlayerScores(cuboid, 2);
            double R = playerScores[0] - (cuboid.volume.dprimitive() / 2);
            // if (DEBUG)
                System.out.printf("R = %f at %d\n", R, i);
            
            if (i == (Main.MAX_TRIES - 160)) {
            // if (R >= Math.min(currentMinimum, i)) {
                return new double[]{R, (double)i};
            }
        }
    }
    
    /**
     * Finds n player scores
     *
     * @param  cuboid  the cuboid on which the game is played
     * @param  players the number of players playing
     * 
     * @see            {@link Main.next}
    */
    public static int[] findPlayerScores(Cuboid cuboid, int players) {
        int[] playerScores = new int[players];
        for (int i = 0; i < Main.MAX_TRIES; ++i) {
            Main.next(cuboid);
            playerScores[i % players] += cuboid.score;
            if (cuboid.positions.stream().allMatch(unit -> unit.state == State.DEAD)) {
                if (DEBUG) {
                    for (int playerIndex = 0; playerIndex < players; ++playerIndex) {
                        System.out.printf("Player %d scored %d\n", playerIndex + 1, playerScores[playerIndex]);
                    }
                }
                break;
            }
        }
        
        return playerScores;
    } 

    /**
     * Returns the least number of required moves 
     *
     * @param  cuboid  the cuboid on which the game is played
     * 
     * @return         the least number of required moves
     * @see            {@link Main.next}
    */
    protected static int MAX_TRIES = 200;
    public static int findLeastNumberOfRequiredMoves(Cuboid cuboid) {
        int i = 1;
        for (; i < Main.MAX_TRIES;) {
            Main.next(cuboid);
            if (cuboid.positions.stream().allMatch(unit -> unit.state == State.DEAD)) {
                break;
            }
            ++i;
        }
        // System.out.println(cuboid.positions);
        return i;
    }
}

/**
 * UnSigned<T extends Number> is a generic class that exists to make 
 * sure all operations are done on non-negative Number.
 *      Because T extends Number, the UnSigned<T> class can wrap Number
 *      and all of its subclasses including:
 *          (1). AtomicInteger
 *          (2). AtomicLong
 *          (3). BigDecimal 
 *          (4). BigInteger 
 *          (5). Byte
 *          (6). Double
 *          (7). Float 
 *          (8). Integer 
 *          (9). Long 
 *         (10). Short
 * 
 * 
 * It was intended as an implementation of the c++ identifier of
 * the same name. It is however crude and currently unfinished.
 * 
 * Since it wraps the boxified version of these types, UnSigned<T>
 * can hold a Number represented with a strictly necessary amount of 
 * memory. The specific amount is not known at compile-time but is 
 * allocated based on the given machines RAM and then freed by the
 * garbage collector
 *  
 *  
 * @author      Clinton
 * @version     %I%, %G%
 * @since       1.0
*/
class UnSigned<T extends Number> {
    /** 
     * Contains original representation of the input data
     *
     * @see             UnSigned#[constructor]
     * @since           1.0
    */
    private T data;

    /** 
     * Contains ByteBuffer of arbitrary size wrapping byte[]
     * representation of {@link Unsigned<T>.data}
     *
     * Not currently used
     * 
     * @see             UnSigned#[constructor]
     * @see             UnSigned<T>.bytes
     * @since           1.0
    */
    private ByteBuffer implementationBuffer;

    /** 
     * Contains byte[] representation of {@link Unsigned<T>.data}
     * 
     * @see             UnSigned#[constructor]
     * @see             UnSigned<T>.implementationBuffer
     * @since           1.0
    */
    private byte [] bytes;

    /** 
     * Contains unsigned transform of 2's compliment
     * representation of {@link Unsigned<T>.data}
     * 
     * @see             UnSigned#[constructor]
     * @see             UnSigned<T>.bytes
     * @see             UnSigned<T>.implementationBuffer
     * @since           1.0
    */
    private long transform;

    /** 
     * Provides primitive long representation of unsigned value
     *
     * @return          Long wrapper of {@link Unsigned<T>.data}
     *                   implicitly converts to primitive type {long}
     *
     * @see             UnSigned<T>.data
     * @since           1.0
     */
    public long primitive() { return this.data.longValue(); }

    /** 
     * Provides primitive double representation of unsigned value
     *
     * @return          Double wrapper of {@link Unsigned<T>.data}
     *                   implicitly converts to primitive type {double}
     *
     * @see             UnSigned<T>.data
     * @since           1.0
    */
    public double dprimitive() { return this.data.doubleValue(); }

    /** 
     * Reverts to input value
     *
     * @return          input value
     *
     * @see             UnSigned<T>.data
     * @since           1.0
    */
    public T revert() { return data; }

    /**
     * Unreachable is a static class that exists to represent
     * useful values that are not necessarily representable values 
     *      It currently wraps:
     *          (1). static final Infinity
     *                  Because it is unsigned it can only be possitive Infinity
     * 
     * 
     *  
     * @see         UnSigned<T>.Infinity
     * 
     * @author      Clinton
     * @version     %I%, %G%
     * @since       1.0
    */
    protected static class Unreachable extends UnSigned<Integer> {
        Unreachable() { 
            super(Integer.valueOf(0));
        }
    }
    public static final UnSigned<?> Infinity = new Unreachable();
    
    /** 
     * Constructs new UnSigned Object
     *
     * @param value     value to be wrapped and marked as unsigned 
     *                  
     * @return          new UnSigned<T> Object from {@link value}
     *
     * @see             Main
     * @since           1.0
    */
    UnSigned(T value) {
        if (value.intValue() < 0)
            throw new IllegalArgumentException("Value is not non-negative", new Throwable());

        this.data = value;
        String binary = Utilities.asBinary(value).toString();

        this.bytes = new BigInteger(binary, 2).toByteArray(); //TODO: Replace with byte[] contructor implementation
        this.implementationBuffer = ByteBuffer.wrap(bytes);

        this.transform = value.intValue() & Long.MAX_VALUE;
    }

    /** 
     * Constructs new UnSigned Object from int primitive
     *
     * @param value     value to be wrapped and marked as unsigned 
     *                  
     * @return          new UnSigned<T> Object from {@link value}
     *
     * @since           1.0
    */
    public static UnSigned<? extends Number> of(int value) {
        return new UnSigned<Integer>(value);
    }

    /** 
     * Constructs new UnSigned Object from long primitive
     *
     * @param value     value to be wrapped and marked as unsigned 
     *                  
     * @return          new UnSigned<T> Object from {@link value}
     *
     * @since           1.0
    */    public static UnSigned<? extends Number> of(long value) {
        return new UnSigned<Long>(value);
    }
    
    /** 
     * Constructs new UnSigned Object from short primitive
     *
     * @param value     value to be wrapped and marked as unsigned 
     *                  
     * @return          new UnSigned<T> Object from {@link value}
     *
     * @since           1.0
    */    public static UnSigned<? extends Number> of(short value) {
        return new UnSigned<Short>(value);
    }
    
    /** 
     * UnSigned's implementation of [<] operator
     *
     * @param comparator value to be compared to {this} 
     *                  
     * @return          {True}  if unsigned comparator's primitive representation 
     *                          is less than {this}'s primitive representation
     *                  {False} otherwise
     *
     * @since           1.0
    */    
    public Boolean isLessThan(UnSigned<T> comparator) {
        return this.transform < comparator.primitive();
    }

    /** 
     * UnSigned's implementation of [>] operator
     *
     * @param comparator value to be compared to {this} 
     *                  
     * @return          {True}  if unsigned comparator's primitive representation 
     *                          is greater than {this}'s primitive representation
     *                  {False} otherwise
     *
     * @since           1.0
    */ 
    public Boolean isGreaterThan(UnSigned<T> comparator) {
        return this.transform > comparator.primitive();
    }

}

/**
 * Utilities is an abstract class that exists to contain operations
 * that are useful but are not specific to Problem 327.
 * 
 * The original version contains many such methods but because they
 * would unecessarily expand the attachment size, I have removed them
 *  
 *  
 * @author      Clinton
 * @version     %I%, %G%
 * @since       1.0
 */
abstract class Utilities {

    /** 
     * Converts value to binary string representaion
     *
     * @param value     value to be converted to String 
     *                  
     * @return          new String Object from the binary 
     *                  representation of {@link value}
     *
     * @see             UnSigned<T>#[constructor]
     * @since           1.0
    */
    public static <T extends Number> String asBinary(T value) {
        return Long.toBinaryString((value.longValue()));
    }
    
}

/**
 * State is an enum that exists to represent the state 
 * of a Unit object
 *      ALIVE: is not among the Unit cubes that have been removed
 *      DEAD:  has been selected either directly or as a result of 
 *             another Unit cube in its axis being picked 
 * 
 * @see Unit 
 *  
 * @author      Clinton
 * @version     %I%, %G%
 * @since       1.0
*/
enum State {
    ALIVE(" "),
    DEAD("x");
    
    String representation;
    State(String value) {
        this.representation = value;
    }
    
    @Override
    public String toString() {
        return this.representation;
    }
}

/**
 * Unit is a class that exists to represent each Unit cube.
 * 
 * It contains its x, y, and z position as well as an instance of 
 * the State enum
 * 
 * @see State
 *  
 * @author      Clinton
 * @version     %I%, %G%
 * @since       1.0
*/
class Unit {
    UnSigned<Integer> xPosition, yPosition, zPosition;
    State state;

    /** 
     * Constructs new Unit Object
     *
     * @param x     x position of unit 
     * @param y     y position of unit 
     * @param z     z position of unit
     * @param state value to represent whether DEAD or ALIVE 
     *                  
     * @return          new Unit Object from {@link x, y, z, state}
     *
     * @see             Cuboid.expandIndex
     * @see             Cuboid.collect
     * @since           1.0
     */
    Unit(int x, int y, int z, State state) {
        this.xPosition = new UnSigned<Integer>(x);
        this.yPosition = new UnSigned<Integer>(y);
        this.zPosition = new UnSigned<Integer>(z);
        this.state = state;
    }

    @Override
    public String toString() {
        // return String.format("[%d|%d|%d]", xPosition.primitive(), yPosition.primitive(), zPosition.primitive());
        return String.format("[%s]", this.state);
    }
}


/**
 * Cuboid is a class that exists to be the rectangular prism
 * on which the operations are being done
 *  *  
 *  
 * @author      Clinton
 * @version     %I%, %G%
 * @since       1.0
 */
class Cuboid {    
    public static boolean DEBUG = false; 

    public UnSigned<Integer> length, width, height;
    public ArrayList<Unit> positions;
    
    protected UnSigned<Integer> volume;
    
    protected int accumulator = 0;
    protected int score;


    /** 
     * Expands ArrayList index into 3-dimensional position on the Cuboid
     * and constructs a Unit object at that position 
     *
     * @param index     value to be expanded
     * @param length    size of z axis of cuboid 
     * @param width     size of x axis of cuboid 
     * @param height    size of y axis of cuboid 
     *                  
     * @return          new Unit Object from {@link index, length, width, height}
     *
     * @see             Cuboid.populate
     * @since           1.0
    */
    // https://stackoverflow.com/questions/7367770/how-to-flatten-or-index-3d-array-in-1d-array
    protected static Unit expandIndex(int index, int length, int width, int height) {
        final int z = index / (width * height);
        index -= (z * width * height);
        final int y = index / width;
        final int x = index % width;
        return new Unit(x, y, z, State.ALIVE);
    }

    /** 
     * Checks if the left-hand-side is always greater than or equal to 0
     * and less than the right-hand-side
     *
     * @param lhs     left hand side
     * @param rhs     right hand side 
     *                  
     * @return        {true}  if the left-hand-side is always greater than or equal to 0
     *                        and less than the right-hand-side
     *                {false} otherwise
     *
     * @see           Cuboid.flattenIndex
     * @see           Cuboid.collect
     * @since         1.0
    */
    protected static boolean constrained(int[] lhs, int[] rhs) {
        if (lhs.length != rhs.length)
            throw new IllegalArgumentException("Comparison cannot happen on arrays of different lengths", new Throwable());

        for (int i = 0; i < lhs.length; i++) {
            if (lhs[i] > rhs[i] || lhs[i] < 0) {
                return false; 
            }
        }
        return true;
    }

    /** 
     * Flattens index from 3-dimensions to ArrayList index 
     *
     * @param coordinates     3-dimensional coordinates in the form [x, y, z] to be converted 
     *                          to ArrayList index
     * @param length          size of z axis of Cuboid
     * @param width           size of x axis of Cuboid
     * @param height          size of y axis of Cuboid 
     *                  
     * @return                new UnSigned<Integer> object containing the flattend index
     *
     * @see           Cuboid.display
     * @see           Cuboid.best
     * @see           Cuboid.collect
     * @since         1.0
    */
    protected static UnSigned<Integer> flattenIndex(int[] coordinates, int length, int width, int height) {
        if (coordinates.length != 3)
            throw new IllegalArgumentException("Cuboids only exist in 3 Dimensions", new Throwable());
        
        if (!Cuboid.constrained(coordinates, new int[] {width, height, length}))
            throw new IllegalArgumentException("Coordinates cannot be beyond the bounds of the Cuboid", new Throwable("Cuboid::constrained line 250"));
        
        Integer flattenedIndex = coordinates[0] + (width * (coordinates[1] + (height * coordinates[2])));
        return new UnSigned<>(flattenedIndex);
    }

    /** 
     * Checks to see if object is already in collection 
     *
     * @param object     object to be found in collection 
     * @param collection collection to be searched
     *                  
     * @return        {null}   if object is in collection
     *                {object} if it isn't
     * 
     * @see           Cuboid.populate
     * @since         1.0
    */
    private static <T> T duplicated(T object, List collection) {
        if (collection.contains(object)) {
            return null;
        }
        return object;
    }

    /** 
     * Populates ArrayList with Units containing a unique [x, y, z] position
     * on a Cuboid
     *
     * @param indexes   ArrayList of indices to be populated 
     * @param size      the size of {@link indexes}
     * @param length    size of z axis of prospective Cuboid
     * @param width     size of x axis of prospective Cuboid
     * @param height    size of y axis of prospective Cuboid
     *                  
     * @return          populated ArrayList<Unit> 
     * 
     * @see           Cuboid.initialize
     * @since         1.0
    */
    protected static ArrayList<Unit> populate(ArrayList<Unit> indexes, int size, int length, int width, int height) {
        for (int i = 0; i < size; ++i) {
            Unit position;
            if ((position = duplicated(Cuboid.expandIndex(i, length, width, height), indexes)) != null)
                indexes.add(position);
        }
        return indexes;
    } 

    /** 
     * Initializes a Cuboid with the given Dimensions
     *
     * @param length    size of z axis of prospective Cuboid
     * @param width     size of x axis of prospective Cuboid
     * @param height    size of y axis of prospective Cuboid
     *                  
     * @return          new Cuboid object
     * 
     * @see           Main
     * @since         1.0
    */
    public static Cuboid initialize(UnSigned<Integer> length, UnSigned<Integer> width, UnSigned<Integer> height) {
        Cuboid instance = new Cuboid();
        instance.length = length;
        instance.width = width;
        instance.height = height;
        instance.volume = new UnSigned<>((int)(length.primitive() * width.primitive() * height.primitive()));
        instance.positions = new ArrayList<Unit>(instance.volume.revert());
        Cuboid.populate(instance.positions, instance.volume.revert(), length.revert(), width.revert(), height.revert());
        return instance;
    }

    protected Cuboid() {

    }

    /** 
     * Displays a snapshot of the game 
     * 
     * @see           Main.next
     * @since         1.0
    */
    public void display() {
        for (int z = 0; z < this.length.primitive(); ++z) {
            for (int y = 0; y < this.height.primitive(); ++y) {
                System.out.print("|| ");
                for (int x = 0; x < this.width.primitive(); ++x) {
                    int index = Cuboid
                                    .flattenIndex(
                                        new int[]{x, y, z}, 
                                        this.length.revert(),
                                        this.width.revert(), 
                                        this.height.revert() 
                                    )
                                    .revert()
                                    .intValue();
                    System.out.print(this.positions.get(index));
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println("------------------------------------");
    }

    /** 
     * Removes(changes the state to DEAD) the Unit cube at the coordinates
     * as well as the Units along the same axes
     *
     * @param coordinates 
     *                  
     * @return        transformed ArrayList<Unit> 
     * 
     * @see           Main
     * @see           Cuboid.best
     * @since         1.0
    */
    public ArrayList<Unit> collect(int[] coordinates) {
        if (coordinates.length != 3)
            throw new IllegalArgumentException("Cuboids only exist in 3 Dimensions", new Throwable());

        if (!Cuboid.constrained(coordinates, new int[] {this.width.revert(), this.height.revert(), this.length.revert()})) {
            throw new IllegalArgumentException("Coordinates cannot be beyond the bounds of the Cuboid", new Throwable("Cuboid::constrained line 250"));
        }

        int x = coordinates[0];        
        int y = coordinates[1];
        int z = coordinates[2];
        ArrayList<Unit> positions = new ArrayList<Unit>(this.positions);
        positions.set(
            Cuboid.flattenIndex(coordinates, this.length.revert(), this.width.revert(), this.height.revert()).revert(),
            new Unit(x, y, z, State.DEAD)
        );
        
        int i = 0;
        for (; i < this.width.revert(); ++i) {
            positions.set(
                Cuboid.flattenIndex(new int[] {i, y, z}, this.length.revert(), this.width.revert(), this.height.revert()).revert(),
                new Unit(i, y, z, State.DEAD)
            );  
        }
        for (i = this.height.revert() - 1; i >= 0; i--) {
            positions.set(
                Cuboid.flattenIndex(new int[] {x, i, z}, this.length.revert(), this.width.revert(), this.height.revert()).revert(),
                new Unit(x, i, z, State.DEAD)
            );  
        }
            
        for (i = 0; i < this.length.revert(); ++i) {
            positions.set(
                Cuboid.flattenIndex(new int[] {x, y, i}, this.length.revert(), this.width.revert(), this.height.revert()).revert(),
                new Unit(x, y, i, State.DEAD)
            );  
        }
        return positions;
    }

    /** 
     * Finds the positions for the best moves and returns the furthest 
     * best move position
     *
     * @param coordinates 
     *                  
     * @return        coordinates of the best move 
     * 
     * @see           Main
     * @see           Cuboid.best
     * @since         1.0
    */
    public int[] best() {
        int[] position = new int[3];
        int b = 0;
        for (int z = 0; z < this.length.primitive(); ++z) {
            for (int y = 0; y < this.height.primitive(); ++y) {
                for (int x = 0; x < this.width.primitive(); ++x) {
                    ArrayList<Unit> transformed = this.collect(new int[]{x, y, z});
                    if (this.positions.get(Cuboid.flattenIndex(new int[]{x, y, z}, this.length.revert(), this.width.revert(), this.height.revert()).revert()).state == State.DEAD)
                        continue;
                    transformed.removeIf(unit -> (unit.state != State.DEAD));
                    if (transformed.size() >= b) {
                        b = transformed.size();
                        position = new int[] {x, y, z};
                    }
                }
            }
        }
        this.score = b - accumulator;
        if (DEBUG)
            System.out.format("(best:: %d)\n", this.score);
        this.accumulator = b;
        if (DEBUG) {
            System.out.format("(position:: ");
            Arrays.stream(position).forEach(element -> System.out.print(element + ", "));
            System.out.format(")\n");
        }
        return position;
    }

}