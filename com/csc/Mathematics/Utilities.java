package com.csc.Mathematics;

import java.math.BigInteger;
import java.util.function.Function;
import java.math.BigDecimal;

/**
 * Utilities is an abstract class that exists to contain operations
 * that are useful but are not specific to Problem 327.
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
