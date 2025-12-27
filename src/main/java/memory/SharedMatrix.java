package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i<matrix.length; i++){
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = newVectors;
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i<matrix.length; i++){
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if (matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }

        int numRows = matrix.length;
        int numCols = matrix[0].length;
    
        SharedVector[] newVectors = new SharedVector[numCols];

        for (int j = 0; j < numCols; j++) {
            double[] columnData = new double[numRows];
            for (int i = 0; i < numRows; i++) {
                columnData[i] = matrix[i][j];
            }
        
        newVectors[j] = new SharedVector(columnData, VectorOrientation.COLUMN_MAJOR);
        }
    
        this.vectors = newVectors;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (this.vectors == null || vectors.length == 0){
            return new double[0][0];
        }

        SharedVector[] currentVecs = this.vectors;
        VectorOrientation orientation = currentVecs[0].getOrientation();
        if (orientation == VectorOrientation.ROW_MAJOR) {
            int numRows = currentVecs.length;
            int numCols = currentVecs[0].length();
            double[][] matrix = new double[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                currentVecs[i].readLock();
                try {
                    for (int j = 0; j < numCols; j++) {
                        matrix[i][j] = currentVecs[i].get(j);
                    }
                } finally {
                    currentVecs[i].readUnlock();
                }
            }
        
            return matrix;
        }
        else {
            int numCols = currentVecs.length;
            int numRows = currentVecs[0].length();
            double[][] matrix = new double[numRows][numCols];

            for (int j = 0; j < numCols; j++) {
                currentVecs[j].readLock();
                try {
                    for (int i = 0; i < numRows; i++) {
                        matrix[i][j] = currentVecs[j].get(i);
                    }
                } finally {
                    currentVecs[j].readUnlock();
                }
            }
            return matrix;
        }
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        if (index >= this.vectors.length || index < 0)
            return null;
        return this.vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return this.vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (this.length() == 0) {throw new IllegalArgumentException("The Matrix is Empty");}
        VectorOrientation orientation = this.vectors[0].getOrientation();
        for (int i = 0; i<this.vectors.length; i++){
            if (this.vectors[i].getOrientation() != orientation){
                throw new IllegalAccessError("a vector isn't orriented correctly");
            }
        }
        return orientation;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (int i = 0; i < vecs.length; i++){
            if (vecs[i] != null) {
                vecs[i].readLock();
            }
        }

    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (int i = 0; i < vecs.length; i++){
            if (vecs[i] != null) {
                vecs[i].readUnlock();
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (int i = 0; i < vecs.length; i++){
            if (vecs[i] != null) {
                vecs[i].writeLock();
            }
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (int i = 0; i < vecs.length; i++){
            if (vecs[i] != null) {
                vecs[i].writeUnlock();
            }
        }
    }
}
