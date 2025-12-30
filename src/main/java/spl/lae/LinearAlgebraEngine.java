package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        // Root Optimization
        while(computationRoot.getNodeType() != parser.ComputationNodeType.MATRIX) {
            parser.ComputationNode nodeToCompute = computationRoot.findResolvable();
            if (nodeToCompute == null) {
                break; // No resolvable nodes found
            }
            loadAndCompute(computationRoot);
            
        }

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        java.util.List<ComputationNode> children = node.getChildren();
        parser.ComputationNodeType type = node.getNodeType();
        double[][] leftData = children.get(0).getMatrix();
        leftMatrix.loadRowMajor(leftData);
        if (children.size() > 1) {
            double[][] rightData = children.get(1).getMatrix();
            if (type == parser.ComputationNodeType.MULTIPLY) {
                rightMatrix.loadColumnMajor(rightData);
            } else {
                rightMatrix.loadRowMajor(rightData);
            }
        }

        // TODO: create compute tasks & submit tasks to executor
        List<Runnable> tasks = null;
        switch (type) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation type: " + type);
        }
        executor.submitAll(tasks);// Submit all tasks to the executor
        double[][] resultMat = leftMatrix.readRowMajor();
        node.resolve(resultMat);
    }
    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numRows = leftMatrix.length();
        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                memory.SharedVector leftRow = leftMatrix.get(rowIndex);
                memory.SharedVector rightRow = rightMatrix.get(rowIndex);
                leftRow.add(rightRow);
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numRows = leftMatrix.length();
        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                memory.SharedVector row = leftMatrix.get(rowIndex);
                row.vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numRows = leftMatrix.length();
        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                memory.SharedVector row = leftMatrix.get(rowIndex);
                row.negate();
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numRows = leftMatrix.length();
        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                memory.SharedVector row = leftMatrix.get(rowIndex);
                row.transpose();
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
}
