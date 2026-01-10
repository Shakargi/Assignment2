package spl.lae;

import java.io.IOException;
import parser.ComputationNode;
import parser.InputParser;
import parser.OutputWriter;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar lga-1.0.jar <numThreads> <input.json> <output.json>");
            System.exit(1);
        }

        int numThreads = 0;
        String inputPath = "";
        String outputPath = "";

        try {
            numThreads = Integer.parseInt(args[0]);
            inputPath = args[1];
            outputPath = args[2];
        } catch (NumberFormatException e) {
            System.err.println("Error: First argument must be an integer representing the number of threads.");
            System.exit(1);
        }

        LinearAlgebraEngine engine = null;

        try {
            engine = new LinearAlgebraEngine(numThreads);

            InputParser parser = new InputParser();
            ComputationNode root = parser.parse(inputPath);

            ComputationNode resultNode = engine.run(root);

            double[][] resultMatrix = resultNode.getMatrix();
            OutputWriter.write(resultMatrix, outputPath);

            System.out.println("Computation finished successfully.");
            System.out.println(engine.getWorkerReport());

        } catch (Exception e) {
            try {
                OutputWriter.write(e.getMessage(), outputPath);
            } catch (IOException ioException) {
                System.err.println("Failed to write error to output file:");
                ioException.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (engine != null) {
                engine.shutdown();
            }
        }
    }
}
