package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.orientation = orientation;
        this.vector = vector;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
            return vector[index];
        } finally {
            readUnlock();
        }

        
    }

    public int length() {
        // TODO: return vector length
        return this.vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        return this.orientation;
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        this.writeLock();
        try{
            if (this.orientation == VectorOrientation.ROW_MAJOR){this.orientation = VectorOrientation.COLUMN_MAJOR;}
            else{this.orientation = VectorOrientation.ROW_MAJOR;}
        } finally {
            this.writeUnlock();
        }
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        this.writeLock();
        other.readLock();
        try {
            if (this.vector.length == other.vector.length){
                for (int i = 0; i < this.vector.length; i++){
                    this.vector[i] += other.get(i);
                }
            }
        } finally {
            other.readUnlock();
            this.writeUnlock();
        }
        
    }

    public void negate() {
        // TODO: negate vector
        this.writeLock();
        try{
            for (int i = 0; i < this.vector.length; i++){
                this.vector[i] = -this.vector[i];
            }
        } finally {
            this.readUnlock();
        }
        
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        this.writeLock();
        other.readLock();
        double result = 0;
        try{
            if (this.vector.length == other.vector.length && this.orientation == VectorOrientation.ROW_MAJOR && this.orientation == VectorOrientation.COLUMN_MAJOR) {
                for (int i = 0; i < this.vector.length; i++){
                    result += this.vector[i]*other.get(i);
                }
            }
            return result;
        } finally {
            this.writeUnlock();
            other.readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        this.writeLock();
        try {
            int numVectors = matrix.length();
            if (numVectors == 0) return;

            VectorOrientation matrixOrientation = matrix.get(0).getOrientation();
            for (int i = 1; i < numVectors; i++) {
                if (matrix.get(i).getOrientation() != matrixOrientation) {
                throw new IllegalArgumentException("Illegal operation: inconsistent matrix orientation");
            }
        }

        double[] tempResult;

        if (matrixOrientation == VectorOrientation.COLUMN_MAJOR) {
            if (this.vector.length != matrix.get(0).length()) {
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }

            tempResult = new double[numVectors];
            for (int j = 0; j < numVectors; j++) {
                tempResult[j] = this.dot(matrix.get(j));
            }
        } 
        else {
            if (this.vector.length != numVectors) {
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }

            int rowWidth = matrix.get(0).length();
            tempResult = new double[rowWidth];

            for (int i = 0; i < numVectors; i++) {
                SharedVector currentRow = matrix.get(i);
                currentRow.readLock();
                try {
                    for (int j = 0; j < rowWidth; j++) {
                        tempResult[j] += this.vector[i] * currentRow.get(i);
                    }
                } finally {
                    currentRow.readUnlock();
                }
            }
        }

        this.vector = tempResult;

        } finally {
            this.writeUnlock();
        }
    }
}
