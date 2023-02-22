package com.csc.Mathematics;

import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.function.Function;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.nio.ByteBuffer;

class Main {
    public static void main(String[] args) {
        UnSigned<Integer> length = new UnSigned<Integer>(3);
        UnSigned<Integer> width = new UnSigned<Integer>(4);
        UnSigned<Integer> height = new UnSigned<Integer>(5);
        Cuboid cuboid = Cuboid.initialize(length, width, height);

        int least = Main.findLeastNumberOfRequiredMoves(cuboid);
        System.out.println(least);
    }

    public static void next(Cuboid cuboid) {
        cuboid.positions = cuboid.collect(cuboid.best());
        cuboid.display();
    }

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


class UnSigned<T extends Number> {

    private T data;

    private ByteBuffer implementationBuffer;
    private byte [] bytes;

    private long transform;

    public long primitive() { return this.data.longValue(); }
    public double dprimitive() { return this.data.doubleValue(); }

    public T revert() { return data; }

    protected static class Unreachable extends UnSigned {
        Unreachable() { 
            super(Integer.valueOf(0));
        }
    }
    public static final UnSigned<?> Infinity = new Unreachable();

    UnSigned(T value) {
        if (value.intValue() < 0)
            throw new IllegalArgumentException("Value is not non-negative", new Throwable());

        this.data = value;
        String binary = Utilities.asBinary(value).toString();

        this.bytes = new BigInteger(binary, 2).toByteArray(); //TODO: Replace with byte[] contructor implementation
        this.implementationBuffer = ByteBuffer.wrap(bytes);

        this.transform = value.intValue() & Long.MAX_VALUE;
    }

    public static UnSigned<? extends Number> of(int value) {
        return new UnSigned<Integer>(value);
    }
    
    public static UnSigned<? extends Number> of(long value) {
        return new UnSigned<Long>(value);
    }
    
    public static UnSigned<? extends Number> of(short value) {
        return new UnSigned<Short>(value);
    }

    public Boolean isLessThan(UnSigned<T> comparator) {
        return this.transform < comparator.primitive();
    }

    public Boolean isGreaterThan(UnSigned<T> comparator) {
        return this.transform > comparator.primitive();
    }

}


abstract class Utilities {

    public static <T extends Number> String asBinary(T value) {
        return Long.toBinaryString((value.longValue()));
    }

    public static BigInteger triangular(UnSigned<Long> n) {
        long primitive = n.primitive();
        if (primitive == 1) 
            return BigInteger.valueOf(primitive);
        
        return BigInteger.valueOf(primitive)
                         .add(
                            Utilities.triangular(
                                new UnSigned<Long>(primitive - 1)
                            )
                         );
    }

    public static <T extends Number> BigDecimal continuousProduct(UnSigned<T> beginning, UnSigned<T> end, Function<BigInteger, BigDecimal> auto) {
        if (beginning.primitive() > end.primitive())
            throw new IllegalArgumentException("Invalid Range", new Throwable());
        
        BigDecimal accumulator = BigDecimal.ONE;
        for (int i = (int)beginning.primitive(); i <= end.primitive(); ++i) {
            accumulator = accumulator.multiply(auto.apply(BigInteger.valueOf(i)));
        }
        return accumulator;
    }

    public static BigDecimal factorialDefinedByTriangularIdentity(UnSigned<Long> n) {
        BigDecimal factor = BigDecimal.valueOf(Math.pow(2, n.revert().doubleValue()));
        BigDecimal product = continuousProduct(new UnSigned<>(1L), new UnSigned<>(n.revert().doubleValue()), index -> {
            return new BigDecimal(
                Utilities.triangular(
                    new UnSigned<Long>((2 * index.longValue()) - 1)
                )
            );
        });
        return product.multiply(factor);
    }

    /******************************************************************************
     *
     *  The Gamma function is defined by
     *      Gamma(x) = integral( t^(x-1) e^(-t), t = 0 .. infinity)
     *
     *  Uses Lanczos approximation formula. See Numerical Recipes 6.1.
     *  https://introcs.cs.princeton.edu/java/91float/Gamma.java.html
     ******************************************************************************/
    public static BigDecimal lnGamma(UnSigned<Complex> n) {
        double tmp = (n.dprimitive() - 0.5) * Math.log(n.dprimitive() + 4.5) - (n.dprimitive() + 4.5);
        double ser = 1.0 + 76.18009173    / (n.dprimitive() + 0)   - 86.50532033    / (n.dprimitive() + 1)
                         + 24.01409822    / (n.dprimitive() + 2)   -  1.231739516   / (n.dprimitive() + 3)
                         +  0.00120858003 / (n.dprimitive() + 4)   -  0.00000536382 / (n.dprimitive() + 5);
        return BigDecimal.valueOf(
            tmp + Math.log(ser * Math.sqrt(2 * Math.PI))
        );
    }
    
    public static BigDecimal factorialDefinedByGammaAnalyticContinuation(UnSigned<Complex> n) {
        return BigDecimal.valueOf(
            Math.exp(lnGamma(n).doubleValue())
        );
    }

    public static BigInteger factorialDefinedByLambdaCalculus(UnSigned<Long> n) {
        if (n.primitive() == 0 || n.primitive() == 1) {
            return BigInteger.ONE;
        }
        return Utilities.factorialDefinedByLambdaCalculus(new UnSigned<Long>(n.primitive() - 1));
    }

    //TODO: Use in implementation of Binomial Coefficient
    private static BigInteger combinationImplementation(UnSigned<Long> givenSet, UnSigned<Long> redundancies, UnSigned<Long> remainder) {
        return factorialDefinedByLambdaCalculus(givenSet)
                    .divide(
                        factorialDefinedByLambdaCalculus(redundancies).multiply(factorialDefinedByLambdaCalculus(remainder))
                    );
    }

    public static BigInteger combinationsWithoutReplacement(UnSigned<Long> givenSet, UnSigned<Long> selectedElements) {
        return combinationImplementation(givenSet, selectedElements, new UnSigned<>(givenSet.primitive() - selectedElements.primitive()));
    }

    public static BigInteger combinationsWithReplacement(UnSigned<Long> givenSet, UnSigned<Long> selectedElements) {
        return combinationImplementation(
            new UnSigned<>(givenSet.primitive() + selectedElements.primitive() - 1), selectedElements, new UnSigned<>(givenSet.primitive() - selectedElements.primitive())
        );
    }

    public static BigInteger permutationsWithoutReplacement(UnSigned<Long> givenSet, UnSigned<Long> selectedElements) {
        return combinationImplementation(givenSet, new UnSigned<Long>(Long.valueOf(1)), new UnSigned<>(givenSet.primitive() - selectedElements.primitive()));
    }

    public static BigInteger permutationsWithReplacement(UnSigned<Long> givenSet, UnSigned<Long> selectedElements) {
        return BigInteger.valueOf((long)Math.pow(givenSet.dprimitive(), selectedElements.dprimitive()));
    }

}

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

class Unit {
    UnSigned<Integer> xPosition, yPosition, zPosition;
    State state;

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

class Cuboid {    

    UnSigned<Integer> volume;
    public UnSigned<Integer> length, width, height;
    
    ArrayList<Unit> positions;
    int best = 0;

    // https://stackoverflow.com/questions/7367770/how-to-flatten-or-index-3d-array-in-1d-array
    protected static Unit expandIndex(int index, int length, int width, int height) {
        final int z = index / (width * height);
        index -= (z * width * height);
        final int y = index / width;
        final int x = index % width;
        return new Unit(x, y, z, State.ALIVE);
    }
    
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

    protected static UnSigned<Integer> flattenIndex(int[] coordinates, int length, int width, int height) {
        if (coordinates.length != 3)
            throw new IllegalArgumentException("Cuboids only exist in 3 Dimensions", new Throwable());
        
        if (!Cuboid.constrained(coordinates, new int[] {width, height, length}))
            throw new IllegalArgumentException("Coordinates cannot be beyond the bounds of the Cuboid", new Throwable("Cuboid::constrained line 250"));
        
        Integer flattenedIndex = coordinates[0] + (width * (coordinates[1] + (height * coordinates[2])));
        return new UnSigned<>(flattenedIndex);
    }

    private static <T> T duplicated(T object, List collection) {
        if (collection.contains(object)) {
            return null;
        }
        return object;
    }

    protected static ArrayList<Unit> populate(ArrayList<Unit> indexes, int size, int length, int width, int height) {
        for (int i = 0; i < size; ++i) {
            Unit position;
            if ((position = duplicated(Cuboid.expandIndex(i, length, width, height), indexes)) != null)
                indexes.add(position);
        }
        return indexes;
    } 

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
        System.out.format("(best:: %d)\n", b - this.best);
        this.best = b;
        System.out.format("(position:: ");
        Arrays.stream(position).forEach(element -> System.out.print(element + ", "));
        System.out.format(")\n");
        return position;
    }

}