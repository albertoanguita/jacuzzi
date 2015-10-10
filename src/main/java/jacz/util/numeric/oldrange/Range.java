package jacz.util.numeric.oldrange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * todo see if we can get rid of initialRange and use static methods. It looks ugly
 */
class Range<T extends RangeInterface<T, Y>, Y extends Comparable<Y>> implements Cloneable, Serializable {

    // min and max of the range (null indicates -/+ infinite)
    protected Y min, max;

    /**
     * Used simply to build other instances of T and perform some simple operations which require an instance of T
     */
    private T initialRange;

    public Range() {
        // not to be used, just to offer a default constructor
    }

    public Range(T range) {
        initialRange = range;
        min = range.getMin();
        max = range.getMax();
    }

    public boolean isEmpty() {
        return (min != null && max != null && min.compareTo(max) > 0);
    }

    public boolean contains(Y value) {
        if (value == null) {
            return false;
        }
        if (isEmpty()) {
            return false;
        }
        if (min == null && max == null) {
            return true;
        } else if (min == null) {
            return value.compareTo(max) <= 0;
        } else if (max == null) {
            return value.compareTo(min) >= 0;
        } else {
            return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        }
    }

    public T intersection(T range) {
        if (isEmpty() || range.isEmpty()) {
            // search for a non-null value an generate an isEmpty range (both min and max are non-null in an isEmpty range)
            return generateEmptyRange();
        }
        int overlapping = overlapping(range);
        if (overlapping == -2) {
            return initialRange.buildInstance(min, range.getMax());
        } else if (overlapping == -1) {
            return initialRange.buildInstance(range.getMin(), range.getMax());
        } else if (overlapping == 0 || overlapping == 1) {
            return initialRange.buildInstance(min, max);
        } else if (overlapping == 2) {
            return initialRange.buildInstance(range.getMin(), max);
        } else {
            return generateEmptyRange();
        }
    }

    public List<T> intersection(Collection<T> ranges) {
        List<T> intersectionList = new ArrayList<T>();
        if (isEmpty()) {
            intersectionList.add(generateEmptyRange());
        } else {
            for (T oneRange : ranges) {
                // todo can thread this part (worth?)
                merge(intersectionList, intersection(oneRange));
            }
            if (intersectionList.size() == 0) {
                intersectionList.add(generateEmptyRange());
            }
        }
        return intersectionList;
    }

    private T generateEmptyRange() {
        return initialRange.buildInstance(initialRange.getZero(), initialRange.previous(initialRange.getZero()));
    }

    public RangeToValueComparison compareTo(Y value) {
        if (value == null || isEmpty()) {
            return RangeToValueComparison.ANY_EMPTY;
        }
        if (min == null && max == null) {
            return RangeToValueComparison.CONTAINS;
        } else if (min == null) {
            if (max.compareTo(value) < 0) {
                return RangeToValueComparison.LEFT;
            } else {
                return RangeToValueComparison.CONTAINS;
            }
        } else if (max == null) {
            if (min.compareTo(value) > 0) {
                return RangeToValueComparison.RIGHT;
            } else {
                return RangeToValueComparison.CONTAINS;
            }
        } else {
            if (min.compareTo(value) > 0) {
                return RangeToValueComparison.RIGHT;
            } else if (max.compareTo(value) < 0) {
                return RangeToValueComparison.LEFT;
            } else {
                return RangeToValueComparison.CONTAINS;
            }
        }
    }

    // todo erase
    public int compareToOld(Y value) {
        if (value == null) {
            return -10;
        }
        if (isEmpty()) {
            return -10;
        }
        if (min == null && max == null) {
            return 0;
        } else if (min == null) {
            if (max.compareTo(value) < 0) {
                return -1;
            } else {
                return 0;
            }
        } else if (max == null) {
            if (min.compareTo(value) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (min.compareTo(value) > 0) {
                return 1;
            } else if (max.compareTo(value) < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }


    /**
     * Indicates the way our given range compares with a given rage. The result is a Comparison value, indicating how
     * our range places <u>with respect</u> to the given range. Example: if we use integer ranges, and our range
     * is [1,2], and we compare it to [4,5], the result will be Comparison.LEFT_NO_CONTACT.
     * <p/>
     *
     * @param range the range to test with our range
     * @return an integer value indicating the different overlapping possibilities:
     *         <ul>
     *         <li>ANY_EMPTY if any of the ranges is isEmpty</li>
     *         <li>LEFT_NO_CONTACT if our range is to the left of range, no overlapping and no contact</li>
     *         <li>LEFT_CONTACT if our range is to the left of range, no overlapping but in contact</li>
     *         <li>LEFT_OVERLAP if our range overlaps with range to the left</li>
     *         <li>INSIDE if our range lies completely inside range</li>
     *         <li>EQUALS if our range is equal to range</li>
     *         <li>CONTAINS if our range completely contains range</li>
     *         <li>RIGHT_OVERLAP if our range overlaps with range to the right</li>
     *         <li>RIGHT_CONTACT if our range is to the right of range, no overlapping but in contact</li>
     *         <li>RIGHT_NO_CONTACT if our range is to the right of range, no overlapping and no contact</li>
     *         </ul>
     */
    public RangeToRangeComparison compareTo(T range) {
        // Tested carefully, all OK. 18-08-2010 by Alberto.

        if (isEmpty() || range.isEmpty()) {
            return RangeToRangeComparison.ANY_EMPTY;
        }
        if (min == null && max == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return RangeToRangeComparison.EQUALS;
            } else {
                return RangeToRangeComparison.CONTAINS;
            }
        } else if (min == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return RangeToRangeComparison.INSIDE;
            } else if (range.getMin() == null) {
                if (max.compareTo(range.getMax()) < 0) {
                    return RangeToRangeComparison.INSIDE;
                } else if (max.compareTo(range.getMax()) > 0) {
                    return RangeToRangeComparison.CONTAINS;
                } else {
                    return RangeToRangeComparison.EQUALS;
                }
            } else if (range.getMax() == null) {
                if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return RangeToRangeComparison.LEFT_CONTACT;
                    } else {
                        return RangeToRangeComparison.LEFT_NO_CONTACT;
                    }
                } else {
                    return RangeToRangeComparison.LEFT_OVERLAP;
                }
            } else {
                if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return RangeToRangeComparison.LEFT_CONTACT;
                    } else {
                        return RangeToRangeComparison.LEFT_NO_CONTACT;
                    }
                } else if (max.compareTo(range.getMax()) < 0) {
                    return RangeToRangeComparison.LEFT_OVERLAP;
                } else {
                    return RangeToRangeComparison.CONTAINS;
                }
            }
        } else if (max == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return RangeToRangeComparison.INSIDE;
            } else if (range.getMax() == null) {
                if (min.compareTo(range.getMin()) > 0) {
                    return RangeToRangeComparison.INSIDE;
                } else if (min.compareTo(range.getMin()) < 0) {
                    return RangeToRangeComparison.CONTAINS;
                } else {
                    return RangeToRangeComparison.EQUALS;
                }
            } else if (range.getMin() == null) {
                if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return RangeToRangeComparison.RIGHT_CONTACT;
                    } else {
                        return RangeToRangeComparison.RIGHT_NO_CONTACT;
                    }
                } else {
                    return RangeToRangeComparison.RIGHT_OVERLAP;
                }
            } else {
                if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return RangeToRangeComparison.RIGHT_CONTACT;
                    } else {
                        return RangeToRangeComparison.RIGHT_NO_CONTACT;
                    }
                } else if (min.compareTo(range.getMin()) > 0) {
                    return RangeToRangeComparison.RIGHT_OVERLAP;
                } else {
                    return RangeToRangeComparison.CONTAINS;
                }
            }
        } else {
            if (range.getMin() == null && range.getMax() == null) {
                return RangeToRangeComparison.INSIDE;
            } else if (range.getMin() == null) {
                if (range.getMax().compareTo(max) >= 0) {
                    return RangeToRangeComparison.INSIDE;
                } else if (range.getMax().compareTo(min) >= 0) {
                    return RangeToRangeComparison.RIGHT_OVERLAP;
                } else {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return RangeToRangeComparison.RIGHT_CONTACT;
                    } else {
                        return RangeToRangeComparison.RIGHT_NO_CONTACT;
                    }
                }
            } else if (range.getMax() == null) {
                if (range.getMin().compareTo(min) <= 0) {
                    return RangeToRangeComparison.INSIDE;
                } else if (range.getMin().compareTo(max) <= 0) {
                    return RangeToRangeComparison.LEFT_OVERLAP;
                } else {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return RangeToRangeComparison.LEFT_CONTACT;
                    } else {
                        return RangeToRangeComparison.LEFT_NO_CONTACT;
                    }
                }
            } else {
                if (min.equals(range.getMin()) && max.equals(range.getMax())) {
                    return RangeToRangeComparison.EQUALS;
                } else if (min.compareTo(range.getMin()) >= 0 && max.compareTo(range.getMax()) <= 0) {
                    return RangeToRangeComparison.INSIDE;
                } else if (min.compareTo(range.getMin()) <= 0 && max.compareTo(range.getMax()) >= 0) {
                    return RangeToRangeComparison.CONTAINS;
                } else if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return RangeToRangeComparison.RIGHT_CONTACT;
                    } else {
                        return RangeToRangeComparison.RIGHT_NO_CONTACT;
                    }
                } else if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return RangeToRangeComparison.LEFT_CONTACT;
                    } else {
                        return RangeToRangeComparison.LEFT_NO_CONTACT;
                    }
                } else if (min.compareTo(range.getMin()) > 0) {
                    return RangeToRangeComparison.RIGHT_OVERLAP;
                } else if (max.compareTo(range.getMax()) < 0) {
                    return RangeToRangeComparison.LEFT_OVERLAP;
                } else {
                    // should never reach here
                    try {
                        throw new Exception("Error in Range.java code!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    // cannot reach here
                    return RangeToRangeComparison.EQUALS;
                }
            }
        }
    }

    /**
     * Indicates the way a given range overlaps with our rage
     * <p/>
     * todo change for compareTo, switch values
     *
     * @param range the range to test with our range
     * @return an integer value indicating the different overlapping possibilities:
     *         <ul>
     *         <li>-10 if any of the ranges is isEmpty</li>
     *         <li>-4 if range is to the left of our range, no overlapping and no contact</li>
     *         <li>-3 if range is to the left of our range, no overlapping but in contact</li>
     *         <li>-2 if range overlaps with our range to the left</li>
     *         <li>-1 if range lies completely inside our range</li>
     *         <li>0 if range is equal to our range</li>
     *         <li>1 if range completely contains our range</li>
     *         <li>2 if range overlaps with our range to the right</li>
     *         <li>3 if range is to the right of our range, no overlapping but in contact</li>
     *         <li>4 if range is to the right of our range, no overlapping and no contact</li>
     *         </ul>
     */
    public int overlapping(T range) {
        if (isEmpty() || range.isEmpty()) {
            return -10;
        }
        if (min == null && max == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (min == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return 1;
            } else if (range.getMin() == null) {
                if (max.compareTo(range.getMax()) < 0) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (range.getMax() == null) {
                if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return 3;
                    } else {
                        return 4;
                    }
                } else {
                    return 2;
                }
            } else {
                if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return 3;
                    } else {
                        return 4;
                    }
                } else if (max.compareTo(range.getMax()) < 0) {
                    return 2;
                } else {
                    return -1;
                }
            }
        } else if (max == null) {
            if (range.getMin() == null && range.getMax() == null) {
                return 1;
            } else if (range.getMax() == null) {
                if (min.compareTo(range.getMin()) > 0) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (range.getMin() == null) {
                if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return -3;
                    } else {
                        return -4;
                    }
                } else {
                    return -2;
                }
            } else {
                if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return -3;
                    } else {
                        return -4;
                    }
                } else if (min.compareTo(range.getMin()) > 0) {
                    return -2;
                } else {
                    return -1;
                }
            }
        } else {
            if (range.getMin() == null && range.getMax() == null) {
                return 1;
            } else if (range.getMin() == null) {
                if (range.getMax().compareTo(max) >= 0) {
                    return 1;
                } else if (range.getMax().compareTo(min) >= 0) {
                    return -2;
                } else {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return -3;
                    } else {
                        return -4;
                    }
                }
            } else if (range.getMax() == null) {
                if (range.getMin().compareTo(min) <= 0) {
                    return 1;
                } else if (range.getMin().compareTo(max) <= 0) {
                    return 2;
                } else {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return 3;
                    } else {
                        return 4;
                    }
                }
            } else {
                if (min.equals(range.getMin()) && max.equals(range.getMax())) {
                    return 0;
                } else if (min.compareTo(range.getMin()) >= 0 && max.compareTo(range.getMax()) <= 0) {
                    return 1;
                } else if (min.compareTo(range.getMin()) <= 0 && max.compareTo(range.getMax()) >= 0) {
                    return -1;
                } else if (min.compareTo(range.getMax()) > 0) {
                    if (min.equals(initialRange.next(range.getMax()))) {
                        return -3;
                    } else {
                        return -4;
                    }
                } else if (max.compareTo(range.getMin()) < 0) {
                    if (max.equals(initialRange.previous(range.getMin()))) {
                        return 3;
                    } else {
                        return 4;
                    }
                } else if (min.compareTo(range.getMin()) > 0) {
                    return -2;
                } else if (max.compareTo(range.getMax()) < 0) {
                    return 2;
                } else {
                    // cannot reach here
                    return 0;
                }
            }
        }
    }

    public List<T> subtract(T range) {
        List<T> rangeList = new ArrayList<T>();
        int overlapping = overlapping(range);
        if (Math.abs(overlapping) >= 3) {
            rangeList.add(initialRange);
        } else if (overlapping == -2) {
            rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
        } else if (overlapping == 2) {
            rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
        } else if (overlapping == -1) {
            if (min.equals(range.getMin())) {
                rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
            } else if (max.equals(range.getMax())) {
                rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
            } else {
                rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
                rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
            }
        }
        return rangeList;
    }

    public List<T> subtract(Collection<T> ranges) {
        List<T> resultList = new ArrayList<T>(1);
        resultList.add(initialRange);
        for (T range : ranges) {
            resultList = subtract(resultList, range);
        }
        return resultList;
    }

    public static <T extends RangeInterface<T, Y>, Y extends Comparable<Y>> List<T> subtract(List<T> ranges, T range) {
        List<T> newRangeList = new ArrayList<T>();
        for (T oneRange : ranges) {
            Range r2 = new Range<T, Y>(oneRange);
            newRangeList.addAll(r2.subtract(range));
        }
        return newRangeList;
    }

    public static <T extends RangeInterface<T, Y>, Y extends Comparable<Y>> List<T> merge(Collection<T> ranges) {
        List<T> mergedRanges = new ArrayList<T>();
        for (T range : ranges) {
            merge(mergedRanges, range);
        }
        return mergedRanges;
    }

    public static <T extends RangeInterface<T, Y>, Y extends Comparable<Y>> void merge(List<T> ranges, T range) {
        if (range.isEmpty()) {
            return;
        }
        for (int i = 0; i < ranges.size(); i++) {
            T oneRange = ranges.get(i);
            int overlapping = (new Range<T, Y>(oneRange)).overlapping(range);
            if (overlapping == -4) {
                // insert to the left and finish
                ranges.add(i, range);
                return;
            } else if (overlapping == -3 || overlapping == -2) {
                // merge with the current range
                ranges.set(i, range.buildInstance(range.getMin(), oneRange.getMax()));
                return;
            } else if (overlapping == 1 || overlapping == 2 || overlapping == 3) {
                Y min;
                if (overlapping == 1) {
                    min = range.getMin();
                } else {
                    min = oneRange.getMin();
                }
                int extendedOverlapping = 0;
                int j;
                for (j = i + 1; j < ranges.size(); j++) {
                    T anotherRange = ranges.get(j);
                    extendedOverlapping = (new Range<T, Y>(anotherRange)).overlapping(range);
                    if (extendedOverlapping == -4 || extendedOverlapping == -3 || extendedOverlapping == -2) {
                        break;
                    }
                }
                // we reached the end of the list --> erase all from i + 1 and merge with i
                if (j == ranges.size()) {
                    for (int k = i + 1; k < ranges.size(); ) {
                        ranges.remove(k);
                    }
                    ranges.set(i, range.buildInstance(min, range.getMax()));
                }
                // merge until range.getMax and erase until j - 1
                else if (extendedOverlapping == -4) {
                    ranges.set(i, range.buildInstance(min, range.getMax()));
                    for (int k = i + 1; k < j; k++) {
                        ranges.remove(i + 1);
                    }
                }
                // merge until ranges.get(j).getMax and erase until j
                else if (extendedOverlapping == -3 || extendedOverlapping == -2) {
                    ranges.set(i, range.buildInstance(min, ranges.get(j).getMax()));
                    for (int k = i + 1; k <= j; k++) {
                        ranges.remove(i + 1);
                    }
                }
                return;
            }
        }
        // reached the end --> append
        ranges.add(range);
    }

    public String toString() {
        String str = "[";
        if (min != null) {
            str += min.toString();
        } else {
            str += "-inf";
        }
        str += ", ";
        if (max != null) {
            str += max.toString();
        } else {
            str += "+inf";
        }
        str += "]";
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;

        Range range = (Range) o;

        if (isEmpty() && range.isEmpty()) {
            return true;
        }
        if (min != null && !min.equals(range.min)) {
            return false;
        } else if (min == null && range.min != null) {
            return false;
        }
        if (max != null && !max.equals(range.max)) {
            return false;
        } else if (max == null && range.max != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (isEmpty()) {
            return new Integer(0).hashCode();
        }
        if (min != null) {
            return min.hashCode();
        } else if (max != null) {
            return max.hashCode();
        } else {
            return new Integer(1).hashCode();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
