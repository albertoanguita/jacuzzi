package jacz.util.numeric;

/**
 * Results of comparisons between two ranges
 */
public enum RangeToRangeComparison {

    ANY_EMPTY,
    LEFT_NO_CONTACT,
    LEFT_CONTACT,
    LEFT_OVERLAP,
    EQUALS,
    INSIDE,
    CONTAINS,
    RIGHT_OVERLAP,
    RIGHT_CONTACT,
    RIGHT_NO_CONTACT
}
