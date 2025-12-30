package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
      // TODO: main
      

// אם קיבלנו פרמטרים אמיתיים (למשל מהבודק האוטומטי), נשתמש בהם
      if (args.length != 3) {
      System.out.println("Usage: java -jar LAE.jar <input.json> <output.json> <numThreads>");
      System.exit(1);
      }

      // 1. Read command-line arguments
      String inputPath = args[0];
      String outputPath = args[1];
      int numThreads = Integer.parseInt(args[2]);
      
      // 2. Parse input JSON to create computation tree
      InputParser parser = new InputParser();
      ComputationNode root = parser.parse(inputPath);

      // 3. Create LinearAlgebraEngine with specified number of threads
      LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);

      // 4. Run computation
      ComputationNode resultNode = engine.run(root);

      // 5. Write output JSON with the result matrix
      double[][] resultMatrix = resultNode.getMatrix();
      OutputWriter.write(resultMatrix, outputPath);

      // 6. Print worker report
      String report = engine.getWorkerReport();
      System.out.println("Computation finished successfully. ");
      System.out.println(report);
    }
  }

