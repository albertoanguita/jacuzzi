package jacz.util.data_types.matrices;

import java.util.ArrayList;

/**
 * This class implements a generic matrix for storing and retrieving objects of any class. The number of dimensions
 * is arbitrary, but must be specified at construction time. A default value can be specified for new cells. Size of
 * the matrix can be dynamically modified, being the new cells filled with the default value (or null if no default
 * value is employed). We can even specify a maxSize of zero for one of the dimensions if we want (in that case, no
 * cells will be contained in the whole matrix.
 * <p/>
 * There are the usual get and set methods for storing and retrieving elements in the matrix.
 * <p/>
 * Care must be taken if T is a mutable object and we use one of its values as default value, since all cells with
 * the default value will point to the same object (thus if that object is modified, all values with default value
 * will be modified as well).
 * <p/>
 * This matrix class does not implement any of the classical matrix operations, since it can store any type of values,
 * not only numbers.
 */
public class Matrix<T> {

    private ArrayList<MatrixElement<T>> elements;

    private final int dimensions;

    private final int[] sizes;

    private final T defaultValue;

    public Matrix(int dimensions) throws IllegalArgumentException {
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimension for matrix must be greater or equal than 1");
        }
        elements = null;
        this.dimensions = dimensions;
        this.sizes = new int[dimensions];
        for (int i = 0; i < dimensions; i++) {
            this.sizes[i] = 0;
        }
        this.defaultValue = null;
    }

    public Matrix(int dimensions, T defaultValue) throws IllegalArgumentException {
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimension for matrix must be greater or equal than 1");
        }
        elements = null;
        this.dimensions = dimensions;
        this.sizes = new int[dimensions];
        for (int i = 0; i < dimensions; i++) {
            this.sizes[i] = 0;
        }
        this.defaultValue = defaultValue;
    }

    public Matrix(int dimensions, int[] sizes) throws IllegalArgumentException {
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimension for matrix must be greater or equal than 1");
        }
        elements = null;
        this.dimensions = dimensions;
        this.sizes = new int[dimensions];
        System.arraycopy(sizes, 0, this.sizes, 0, dimensions);
        this.defaultValue = null;
    }

    public Matrix(int dimensions, T defaultValue, int[] sizes) throws IllegalArgumentException {
        if (dimensions < 1) {
            throw new IllegalArgumentException("Dimension for matrix must be greater or equal than 1");
        }
        elements = null;
        this.dimensions = dimensions;
        this.sizes = new int[dimensions];
        System.arraycopy(sizes, 0, this.sizes, 0, dimensions);
        this.defaultValue = defaultValue;
    }

    private void redimensionElements() {
        if (checkForNullDimensions()) {
            elements = null;
        } else {
            if (elements == null) {
                // create new
                elements = new ArrayList<MatrixElement<T>>(sizes[0]);
                for (int i = 0; i < sizes[0]; i++) {
                    elements.add(generateElement(0));
                }
            } else {
                // redimension existing
                if (sizes[0] > elements.size()) {
                    // larger
                    for (MatrixElement<T> matrixElement : elements) {
                        redimensionElements(matrixElement, 0);
                    }
                    for (int i = elements.size(); i < sizes[0]; i++) {
                        elements.add(generateElement(0));
                    }
                } else {
                    // smaller
                    for (int i = 0; i < sizes[0]; i++) {
                        MatrixElement<T> matrixElement = elements.get(i);
                        redimensionElements(matrixElement, 0);
                    }
                    for (int i = sizes[0]; i < elements.size(); i++) {
                        elements.remove(sizes[0]);
                    }
                }
            }
        }
    }

    private MatrixElement<T> generateElement(int dimCount) {
        if (dimCount == dimensions - 1) {
            return new MatrixElement<T>(defaultValue);
        } else {
            dimCount++;
            ArrayList<MatrixElement<T>> elementList = new ArrayList<MatrixElement<T>>(dimCount);
            for (int i = 0; i < sizes[dimCount]; i++) {
                elementList.add(generateElement(dimCount));
            }
            return new MatrixElement<T>(elementList);
        }
    }

    private void redimensionElements(MatrixElement<T> parentMatrixElement, int dimCount) {
        if (dimCount != dimensions - 1) {
            dimCount++;
            ArrayList<MatrixElement<T>> dimElements = parentMatrixElement.getAdditionalDimension();
            if (sizes[dimCount] > dimElements.size()) {
                // larger
                for (MatrixElement<T> matrixElement : dimElements) {
                    redimensionElements(matrixElement, dimCount);
                }
                for (int i = dimElements.size(); i < sizes[dimCount]; i++) {
                    dimElements.add(generateElement(dimCount));
                }
            } else {
                // smaller
                for (int i = 0; i < sizes[dimCount]; i++) {
                    MatrixElement<T> matrixElement = dimElements.get(i);
                    redimensionElements(matrixElement, dimCount);
                }
                for (int i = sizes[dimCount]; i < dimElements.size(); i++) {
                    dimElements.remove(sizes[dimCount]);
                }
            }
        }
        // else, nothing to do here, elements are simply not modified
    }

    private boolean checkForNullDimensions() {
        for (int i = 0; i < dimensions; i++) {
            if (sizes[i] == 0) {
                return true;
            }
        }
        return false;
    }

    public void setDimensionSizes(int... sizes) {
        if (sizes.length == dimensions) {
            System.arraycopy(sizes, 0, this.sizes, 0, dimensions);
            redimensionElements();
        } else {
            throw new RuntimeException("Wrong dimension sizes received. Length of received sizes is " + sizes.length + ", but matrix has " + dimensions + " dimensions!");
        }
    }

    public T get(int i, int... restDim) {
        checkCorrectDimensions(restDim.length + 1);
        MatrixElement<T> matrixElement = elements.get(i);
        for (int j : restDim) {
            matrixElement = matrixElement.getAdditionalDimension().get(j);
        }
        return matrixElement.getSimpleElement();
    }

    public void set(T value, int i, int... restDim) {
        checkCorrectDimensions(restDim.length + 1);
        MatrixElement<T> matrixElement = elements.get(i);
        for (int j : restDim) {
            matrixElement = matrixElement.getAdditionalDimension().get(j);
        }
        matrixElement.setSimpleElement(value);
    }

    private void checkCorrectDimensions(int dim) {
        if (dim != dimensions) {
            throw new IllegalArgumentException("Wrong number of dimensions received: " + dim + ", expected " + dimensions);
        }
    }
}
