package org.aanguita.jacuzzi.data_types.matrices;

import java.util.ArrayList;

/**
 * This class represents an element or list of elements in a matrix. In case or list of elements, such elements can
 * contain themselves more lists. It is used in the matrix implementation.
 */
class MatrixElement<T> {

    private T simpleElement;

    private ArrayList<MatrixElement<T>> additionalDimension;

    public MatrixElement(T simpleElement) {
        this.simpleElement = simpleElement;
        additionalDimension = null;
    }

    public MatrixElement(ArrayList<MatrixElement<T>> additionalDimension) {
        simpleElement = null;
        this.additionalDimension = additionalDimension;
    }

    public T getSimpleElement() {
        return simpleElement;
    }

    public void setSimpleElement(T simpleElement) {
        if (additionalDimension != null) {
            throw new IllegalStateException("Cannot set a new simple value, this matrix element contains an array of values");
        }
        this.simpleElement = simpleElement;
    }

    public ArrayList<MatrixElement<T>> getAdditionalDimension() {
        return additionalDimension;
    }
}
