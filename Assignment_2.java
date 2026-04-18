import java.io.*;
import java.util.*;
import java.lang.*;

public class Assignment_2 {

    // Set Up
    private final static Map<String, Float> optimums = new HashMap<String, Float>();

    private static class Result {
        String problemInstance;
        String algorithm;
        int seed;
        String bestSolution;
        float knownOptimum;
        double runtimeSeconds;
        float totalValue;

        Result(String problemInstance, String algorithm, int seed, String bestSolution,
               float knownOptimum, double runtimeSeconds, float totalValue) {
            this.problemInstance = problemInstance;
            this.algorithm = algorithm;
            this.seed = seed;
            this.bestSolution = bestSolution;
            this.knownOptimum = knownOptimum;
            this.runtimeSeconds = runtimeSeconds;
            this.totalValue = totalValue;
        }
    }

    private static List<Item> ReadInstance(String filename) {
        List<Item> instance = new ArrayList<Item>();
        try (BufferedReader br = new BufferedReader(new FileReader("Knapsack Instances/" + filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                float v = Float.parseFloat(parts[0]);
                float w = Float.parseFloat(parts[1]);
                instance.add(new Item(w, v));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private static void LoadOptimums() {
        optimums.put("f1_l-d_kp_10_269", (float) 295.0);
        optimums.put("f2_l-d_kp_20_878", (float) 1024);
        optimums.put("f3_l-d_kp_4_20", (float) 35);
        optimums.put("f4_l-d_kp_4_11", (float) 23);
        optimums.put("f5_l-d_kp_15_375", (float) 481.0694);
        optimums.put("f6_l-d_kp_10_60", (float) 52);
        optimums.put("f7_l-d_kp_7_50", (float) 107);
        optimums.put("knapPI_1_100_1000_1", (float) 9147);
        optimums.put("f8_l-d_kp_23_10000", (float) 9767);
        optimums.put("f9_l-d_kp_5_80", (float) 130);
        optimums.put("f10_l-d_kp_20_879", (float) 1025);
    }

    //Genetics
    private static void Genetics(List<Item> instances){
        //Initializing population
        long startTime = System.nanoTime();
        int seed= instances.size();
        float maxWheight= instances.get(0).wheight;
        Random rand = new Random(seed);
        // Populatoin
        int populationSize=seed*10;
        final int MAX_POPULATION= seed*20;
        Integer[][] population= new Integer[populationSize][seed];
        for(int i=0;i<populationSize;i++){
            for(int j=0;j<seed;j++){
                int bitCode= rand.nextInt();
                population[i][j]=Math.abs(bitCode %2) ;
            }
        }

        Integer[] best= new Integer[seed];
        float bestFitness=-1;
        int iteration=1;
        int crossOverPoint1=seed/3;
        int crossOverPoint2=(seed/3)*2;

        while(iteration <=800){

            // Find best in population and collect valid
            int validCount = 0;
            List<Integer[]> validGenes= new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                if (population[i] == null)
                    continue;
                if (population[i][0] == null)
                    continue;
                float currFitness = Fitness(population[i], maxWheight, instances);
                if (currFitness >= 0) {
                    validGenes.add(population[i]);
                    validCount++;
                    if (currFitness >= bestFitness) {
                        bestFitness = currFitness;
                        best = population[i];
                    }
                }
            }

            // --- Crossover: produce a new population from pairs of parents ---
            Integer[][] newPopulation = new Integer[populationSize][seed];
            int childIndex = 0;

            // Tournament
            for (int i = 0; i < populationSize; i += 2) {
                // --- Tournament for Parent A ---
                // Individual i competes with individual i+1
                // The one with the higher fitness value wins
                float fitnessA = Fitness(population[i], maxWheight, instances);
                float fitnessB = Fitness(population[(i + 1) % populationSize], maxWheight, instances);
                int parentAIdx = (fitnessA > fitnessB) ? i : (i + 1) % populationSize;

                // --- Tournament for Parent B ---
                // Individual i+2 competes with individual i+3
                // The % ensures we wrap around and never go out of bounds
                int secondPair = (i + 2) % populationSize;
                if (population[secondPair] == null || population[(secondPair + 1) % populationSize] == null)
                    continue;

                float fitnessC = Fitness(population[secondPair], maxWheight, instances);
                float fitnessD = Fitness(population[(secondPair + 1) % populationSize], maxWheight, instances);
                int parentBIdx = (fitnessC > fitnessD) ? secondPair : (secondPair + 1) % populationSize;

                // If both tournaments picked the same individual, shift parentB by 1
                // to ensure the two parents are always genetically distinct
                if (parentAIdx == parentBIdx)
                    parentBIdx = (parentBIdx + 1) % populationSize;

                Integer[] parentA = population[parentAIdx];
                Integer[] parentB = population[parentBIdx];

                // --- Crossover ---
                Integer[] child1 = new Integer[seed];
                Integer[] child2 = new Integer[seed];

                for (int j = 0; j < seed; j++) {
                    if (j < crossOverPoint1 || j>crossOverPoint2) {
                        child1[j] = parentA[j];
                        child2[j] = parentB[j];
                    } else {
                        child1[j] = parentB[j];
                        child2[j] = parentA[j];
                    }
                }

                // --- Mutation ---
                // Check if we are stuck
                boolean isStuck = (iteration > 50 && bestFitness < 0);
                float mutationRate = 0.05f;

                for (int j = 0; j < seed; j++) {
                    float roll = rand.nextFloat();
                    if (isStuck) {
                        // Force weight reduction: High chance to drop an item, 0 chance to add one
                        if (child1[j] == 1 && roll < 0.30f) child1[j] = 0;
                        if (child2[j] == 1 && roll < 0.30f) child2[j] = 0;
                    } else {
                        // Normal 5% bit-flip mutation
                        if (roll < mutationRate) child1[j] = 1 - child1[j];
                        if (roll < mutationRate) child2[j] = 1 - child2[j];
                    }
                }

                // Place children into the new population
                if (childIndex + 1 < newPopulation.length) {
                    newPopulation[childIndex]     = child1;
                    newPopulation[childIndex + 1] = child2;
                    childIndex += 2;
                }
            }
            // --- Survival of the Fittest ---
            List<Integer[]> combinedPool = new ArrayList<>(validGenes);

            for (int j = 0; j < childIndex; j++) {
                if (newPopulation[j] != null) {
                    combinedPool.add(newPopulation[j]);
                }
            }

            combinedPool.sort((geneA, geneB) -> {
                float fitnessA = Fitness(geneA, maxWheight, instances);
                float fitnessB = Fitness(geneB, maxWheight, instances);
                return Float.compare(fitnessB, fitnessA);
            });

            //making sure that population size is valid
            int newSize = Math.min(MAX_POPULATION, combinedPool.size());

            population = new Integer[MAX_POPULATION][seed];
            for (int j = 0; j < MAX_POPULATION && j < combinedPool.size(); j++) {
                population[j] = combinedPool.get(j);
            }

            populationSize = newSize;
            iteration++;


        }
        long endTime = System.nanoTime();
        double runtimeSeconds = (endTime - startTime) / 1_000_000_000.0;

        if(bestFitness<0)
            System.out.println("No solution found \n" );
        else {
            System.out.println("Seed: " + seed);
            System.out.print("GA Solution: ");
            for(int i=0; i<best.length;i++)
                System.out.print(best[i]);
            System.out.println();
            System.out.println("GA Total value: " + bestFitness);
            System.out.println("GA Runtime (seconds): " + runtimeSeconds);
            System.out.println();
        }
    }

    private static String geneToString(Integer[] bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bits.length; i++) {
            sb.append(bits[i]);
        }
        return sb.toString();
    }

    // Genetics
    private static Result Genetics(String filename, List<Item> instances) {
        long startTime = System.nanoTime();

        int seed = instances.size();
        float maxWheight = instances.get(0).wheight;
        Random rand = new Random(seed);

        int populationSize = seed * 10;
        final int MAX_POPULATION = seed * 20;
        Integer[][] population = new Integer[populationSize][seed];

        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < seed; j++) {
                int bitCode = rand.nextInt();
                population[i][j] = Math.abs(bitCode % 2);
            }
        }

        Integer[] best = new Integer[seed];
        for (int i = 0; i < seed; i++) {
            best[i] = 0;
        }

        float bestFitness = -1;
        int iteration = 1;
        int crossOverPoint1 = seed / 3;
        int crossOverPoint2 = (seed / 3) * 2;

        while (iteration <= 800) {
            int validCount = 0;
            List<Integer[]> validGenes = new ArrayList<Integer[]>();

            for (int i = 0; i < populationSize; i++) {
                if (population[i] == null)
                    continue;
                if (population[i][0] == null)
                    continue;

                float currFitness = Fitness(population[i], maxWheight, instances);
                if (currFitness >= 0) {
                    validGenes.add(population[i]);
                    validCount++;
                    if (currFitness >= bestFitness) {
                        bestFitness = currFitness;
                        best = population[i];
                    }
                }
            }

            Integer[][] newPopulation = new Integer[populationSize][seed];
            int childIndex = 0;

            for (int i = 0; i < populationSize; i += 2) {
                float fitnessA = Fitness(population[i], maxWheight, instances);
                float fitnessB = Fitness(population[(i + 1) % populationSize], maxWheight, instances);
                int parentAIdx = (fitnessA > fitnessB) ? i : (i + 1) % populationSize;

                int secondPair = (i + 2) % populationSize;
                if (population[secondPair] == null || population[(secondPair + 1) % populationSize] == null)
                    continue;

                float fitnessC = Fitness(population[secondPair], maxWheight, instances);
                float fitnessD = Fitness(population[(secondPair + 1) % populationSize], maxWheight, instances);
                int parentBIdx = (fitnessC > fitnessD) ? secondPair : (secondPair + 1) % populationSize;

                if (parentAIdx == parentBIdx)
                    parentBIdx = (parentBIdx + 1) % populationSize;

                Integer[] parentA = population[parentAIdx];
                Integer[] parentB = population[parentBIdx];

                Integer[] child1 = new Integer[seed];
                Integer[] child2 = new Integer[seed];

                for (int j = 0; j < seed; j++) {
                    if (j < crossOverPoint1 || j > crossOverPoint2) {
                        child1[j] = parentA[j];
                        child2[j] = parentB[j];
                    } else {
                        child1[j] = parentB[j];
                        child2[j] = parentA[j];
                    }
                }

                boolean isStuck = (iteration > 50 && bestFitness < 0);
                float mutationRate = 0.05f;

                for (int j = 0; j < seed; j++) {
                    float roll = rand.nextFloat();
                    if (isStuck) {
                        if (child1[j] == 1 && roll < 0.30f) child1[j] = 0;
                        if (child2[j] == 1 && roll < 0.30f) child2[j] = 0;
                    } else {
                        if (roll < mutationRate) child1[j] = 1 - child1[j];
                        if (roll < mutationRate) child2[j] = 1 - child2[j];
                    }
                }

                if (childIndex + 1 < newPopulation.length) {
                    newPopulation[childIndex] = child1;
                    newPopulation[childIndex + 1] = child2;
                    childIndex += 2;
                }
            }

            List<Integer[]> combinedPool = new ArrayList<Integer[]>(validGenes);

            for (int j = 0; j < childIndex; j++) {
                if (newPopulation[j] != null) {
                    combinedPool.add(newPopulation[j]);
                }
            }

            Collections.sort(combinedPool, new Comparator<Integer[]>() {
                @Override
                public int compare(Integer[] geneA, Integer[] geneB) {
                    float fitnessA = Fitness(geneA, maxWheight, instances);
                    float fitnessB = Fitness(geneB, maxWheight, instances);
                    return Float.compare(fitnessB, fitnessA);
                }
            });

            int newSize = Math.min(MAX_POPULATION, combinedPool.size());

            population = new Integer[MAX_POPULATION][seed];
            for (int j = 0; j < MAX_POPULATION && j < combinedPool.size(); j++) {
                population[j] = combinedPool.get(j);
            }

            populationSize = newSize;
            iteration++;
        }

        long endTime = System.nanoTime();
        double runtimeSeconds = (endTime - startTime) / 1_000_000_000.0;

        String solutionString;
        if (bestFitness < 0) {
            solutionString = "No solution found";
        } else {
            solutionString = geneToString(best);
        }

        return new Result(
                filename,
                "GA",
                seed,
                solutionString,
                optimums.get(filename),
                runtimeSeconds,
                bestFitness
        );
    }

    // Genetics Helpers
    private static float Fitness(Integer[] bits, float maxWheight, List<Item> instances) {
        float wheight = 0;
        float value = 0;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 1) {
                wheight += instances.get(i).wheight;
                value += instances.get(i).value;
            }
        }
        if (wheight > maxWheight)
            return -1;
        else
            return value;
    }

    // Iterated Local Search
    private static Result ILS(String filename, List<Item> instances) {
        long startTime = System.nanoTime();

        int seed = instances.size();
        Random rand = new Random(seed);

        float maxWeight = instances.get(0).wheight;
        int n = instances.size();

        Integer[] current = new Integer[n];
        for (int i = 0; i < n; i++) {
            current[i] = 0;
        }

        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        Collections.shuffle(order, rand);

        float currentWeight = 0;
        for (int idx = 0; idx < order.size(); idx++) {
            int index = order.get(idx);
            if (rand.nextBoolean() && currentWeight + instances.get(index).wheight <= maxWeight) {
                current[index] = 1;
                currentWeight += instances.get(index).wheight;
            }
        }

        boolean improved = true;
        while (improved) {
            improved = false;

            float currentFitness = 0;
            float currentValidWeight = 0;
            for (int i = 0; i < n; i++) {
                if (current[i] == 1) {
                    currentValidWeight += instances.get(i).wheight;
                    currentFitness += instances.get(i).value;
                }
            }
            if (currentValidWeight > maxWeight) {
                currentFitness = -1;
            }

            float bestNeighbourFitness = currentFitness;
            Integer[] bestNeighbour = new Integer[n];
            for (int i = 0; i < n; i++) {
                bestNeighbour[i] = current[i];
            }

            for (int i = 0; i < n; i++) {
                Integer[] neighbour = new Integer[n];
                for (int j = 0; j < n; j++) {
                    neighbour[j] = current[j];
                }

                neighbour[i] = 1 - neighbour[i];

                float weight = 0;
                float value = 0;
                for (int j = 0; j < n; j++) {
                    if (neighbour[j] == 1) {
                        weight += instances.get(j).wheight;
                        value += instances.get(j).value;
                    }
                }

                if (weight > maxWeight) {
                    continue;
                }

                if (value > bestNeighbourFitness) {
                    bestNeighbourFitness = value;
                    for (int j = 0; j < n; j++) {
                        bestNeighbour[j] = neighbour[j];
                    }
                }
            }

            if (bestNeighbourFitness > currentFitness) {
                for (int i = 0; i < n; i++) {
                    current[i] = bestNeighbour[i];
                }
                improved = true;
            }
        }

        Integer[] best = new Integer[n];
        for (int i = 0; i < n; i++) {
            best[i] = current[i];
        }

        float bestFitness = 0;
        for (int i = 0; i < n; i++) {
            if (best[i] == 1) {
                bestFitness += instances.get(i).value;
            }
        }

        int maxIterations = 2500;

        for (int iter = 0; iter < maxIterations; iter++) {
            Integer[] candidate = new Integer[n];
            for (int i = 0; i < n; i++) {
                candidate[i] = current[i];
            }

            int first = rand.nextInt(n);
            int second = rand.nextInt(n);
            while (second == first) {
                second = rand.nextInt(n);
            }

            candidate[first] = 1 - candidate[first];
            candidate[second] = 1 - candidate[second];

            float candidateWeight = 0;
            float candidateValue = 0;
            for (int i = 0; i < n; i++) {
                if (candidate[i] == 1) {
                    candidateWeight += instances.get(i).wheight;
                    candidateValue += instances.get(i).value;
                }
            }

            if (candidateWeight > maxWeight) {
                continue;
            }

            boolean candidateImproved = true;
            while (candidateImproved) {
                candidateImproved = false;

                float bestLocalFitness = candidateValue;
                Integer[] bestLocal = new Integer[n];
                for (int i = 0; i < n; i++) {
                    bestLocal[i] = candidate[i];
                }

                for (int i = 0; i < n; i++) {
                    Integer[] neighbour = new Integer[n];
                    for (int j = 0; j < n; j++) {
                        neighbour[j] = candidate[j];
                    }

                    neighbour[i] = 1 - neighbour[i];

                    float weight = 0;
                    float value = 0;
                    for (int j = 0; j < n; j++) {
                        if (neighbour[j] == 1) {
                            weight += instances.get(j).wheight;
                            value += instances.get(j).value;
                        }
                    }

                    if (weight > maxWeight) {
                        continue;
                    }

                    if (value > bestLocalFitness) {
                        bestLocalFitness = value;
                        for (int j = 0; j < n; j++) {
                            bestLocal[j] = neighbour[j];
                        }
                    }
                }

                if (bestLocalFitness > candidateValue) {
                    for (int i = 0; i < n; i++) {
                        candidate[i] = bestLocal[i];
                    }

                    candidateValue = 0;
                    for (int i = 0; i < n; i++) {
                        if (candidate[i] == 1) {
                            candidateValue += instances.get(i).value;
                        }
                    }

                    candidateImproved = true;
                }
            }

            float currentValue = 0;
            float currentW = 0;
            for (int i = 0; i < n; i++) {
                if (current[i] == 1) {
                    currentW += instances.get(i).wheight;
                    currentValue += instances.get(i).value;
                }
            }
            if (currentW > maxWeight) {
                currentValue = -1;
            }

            if (candidateValue >= currentValue) {
                for (int i = 0; i < n; i++) {
                    current[i] = candidate[i];
                }
            }

            if (candidateValue > bestFitness) {
                bestFitness = candidateValue;
                for (int i = 0; i < n; i++) {
                    best[i] = candidate[i];
                }
            }
        }

        long endTime = System.nanoTime();
        double runtimeSeconds = (endTime - startTime) / 1_000_000_000.0;

        return new Result(
                filename,
                "ILS",
                seed,
                geneToString(best),
                optimums.get(filename),
                runtimeSeconds,
                bestFitness
        );
    }

    private static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private static String borderLine(int w1, int w2, int w3, int w4, int w5, int w6, int w7) {
        return "+"
                + repeatChar('-', w1 + 2) + "+"
                + repeatChar('-', w2 + 2) + "+"
                + repeatChar('-', w3 + 2) + "+"
                + repeatChar('-', w4 + 2) + "+"
                + repeatChar('-', w5 + 2) + "+"
                + repeatChar('-', w6 + 2) + "+"
                + repeatChar('-', w7 + 2) + "+";
    }

    public static void main(String[] args) {
        LoadOptimums();

        List<Result> results = new ArrayList<Result>();

        for (Map.Entry<String, Float> entry : optimums.entrySet()) {
            String filename = entry.getKey();
            List<Item> instances = ReadInstance(filename);

            Result ilsResult = ILS(filename, instances);
            Result gaResult = Genetics(filename, instances);

            results.add(ilsResult);
            results.add(gaResult);
        }

        int wProblem = "Problem Instance".length();
        int wAlgorithm = "Algorithm".length();
        int wSeed = "Seed Value".length();
        int wBest = "Best Solution".length();
        int wKnown = "Known Optimum".length();
        int wRuntime = "Runtime (seconds)".length();
        int wTotal = "Total value".length();

        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);
            if (r.problemInstance.length() > wProblem) wProblem = r.problemInstance.length();
            if (r.algorithm.length() > wAlgorithm) wAlgorithm = r.algorithm.length();
            if (String.valueOf(r.seed).length() > wSeed) wSeed = String.valueOf(r.seed).length();
            if (r.bestSolution.length() > wBest) wBest = r.bestSolution.length();
            if (String.format("%.4f", r.knownOptimum).length() > wKnown) wKnown = String.format("%.4f", r.knownOptimum).length();
            if (String.format("%.6f", r.runtimeSeconds).length() > wRuntime) wRuntime = String.format("%.6f", r.runtimeSeconds).length();
            if (String.format("%.4f", r.totalValue).length() > wTotal) wTotal = String.format("%.4f", r.totalValue).length();
        }

        String border = borderLine(wProblem, wAlgorithm, wSeed, wBest, wKnown, wRuntime, wTotal);

        String format = "| %-" + wProblem + "s "
                + "| %-" + wAlgorithm + "s "
                + "| %-" + wSeed + "s "
                + "| %-" + wBest + "s "
                + "| %-" + wKnown + "s "
                + "| %-" + wRuntime + "s "
                + "| %-" + wTotal + "s |%n";

        String rowFormat = "| %-" + wProblem + "s "
                + "| %-" + wAlgorithm + "s "
                + "| %-" + wSeed + "d "
                + "| %-" + wBest + "s "
                + "| %-" + wKnown + ".4f "
                + "| %-" + wRuntime + ".6f "
                + "| %-" + wTotal + ".4f |%n";

        System.out.println(border);
        System.out.printf(format,
                "Problem Instance",
                "Algorithm",
                "Seed Value",
                "Best Solution",
                "Known Optimum",
                "Runtime (seconds)",
                "Total value"
        );
        System.out.println(border);

        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);

            String problemName = r.problemInstance;
            if (i % 2 == 1) {
                problemName = "";
            }

            System.out.printf(rowFormat,
                    problemName,
                    r.algorithm,
                    r.seed,
                    r.bestSolution,
                    r.knownOptimum,
                    r.runtimeSeconds,
                    r.totalValue
            );

            if (i % 2 == 1) {
                System.out.println(border);
            }
        }
    }
}