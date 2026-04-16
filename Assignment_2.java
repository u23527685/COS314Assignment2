import java.io.*;
import java.util.*;
import java.lang.*;

public class Assignment_2 {

    // Set Up
    private final static Map<String,Float> optimums= new HashMap<>();

    private static List<Item> ReadInstance(String filename){
        List<Item>instance= new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Knapsack Instances/"+filename))){
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                float v = Float.parseFloat(parts[0]);
                float w = Float.parseFloat(parts[1]);
                instance.add(new Item(w,v));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private static void LoadOptimums(){
        optimums.put("f1_l-d_kp_10_269",(float)295.0);
        optimums.put("f2_l-d_kp_20_878",(float)1024);
        optimums.put("f3_l-d_kp_4_20",(float)35);
        optimums.put("f4_l-d_kp_4_11",(float)23);
        optimums.put("f5_l-d_kp_15_375",(float)481.0694);
        optimums.put("f6_l-d_kp_10_60",(float)52);
        optimums.put("f7_l-d_kp_7_50",(float)107);
        optimums.put("knapPI_1_100_1000_1",(float)9147);
        optimums.put("f8_l-d_kp_23_10000",(float)9767);
        optimums.put("f9_l-d_kp_5_80",(float)130);
        optimums.put("f10_l-d_kp_20_879",(float)1025);
    }

    //Genetics
    private static void Genetics(){}

    // Genetics Helpers

    //Iterated Local Search
    /*
    Knapsack case study using ILS steps from search and optimisation textbook:
    1) solution representation
    2)Initial solution generation
    3)Local search
    4)Pertubation
    5)Local search again
    6)acceptance criterion
    7)Termination condition
     */
    private static void ILS(List<Item> instances) {

        long startTime = System.nanoTime();

        int seed = instances.size();
        Random rand = new Random(seed);

        float maxWeight = instances.get(0).wheight;
        int n = instances.size();

        // 1) Solution representation
        Integer[] current = new Integer[n];
        for (int i = 0; i < n; i++) {
            current[i] = 0;
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        Collections.shuffle(order, rand);

        //2) Initial solution generation
        float currentWeight = 0;
        for (int index : order) {
            if (rand.nextBoolean() && currentWeight + instances.get(index).wheight <= maxWeight) {
                current[index] = 1;
                currentWeight += instances.get(index).wheight;
            }
        }

        // 3) Local search
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

                // textbook local search: flip one bit
                neighbour[i] = 1 - neighbour[i];

                float weight = 0;
                float value = 0;
                for (int j = 0; j < n; j++) {
                    if (neighbour[j] == 1) {
                        weight += instances.get(j).wheight;
                        value += instances.get(j).value;
                    }
                }

                // reject overweight neighbour
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
        float bestWeight = 0;
        for (int i = 0; i < n; i++) {
            if (best[i] == 1) {
                bestWeight += instances.get(i).wheight;
                bestFitness += instances.get(i).value;
            }
        }
        if (bestWeight > maxWeight) {
            bestFitness = -1;
        }

        int maxIterations = 300;

        // 7) Termination condition: maximum of 300 iterations
        for (int iter = 0; iter < maxIterations; iter++) {

            Integer[] candidate = new Integer[n];
            for (int i = 0; i < n; i++) {
                candidate[i] = current[i];
            }

            // 4) Perturbation: flip exactly two bits
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

            // reject perturbed solution if overweight
            if (candidateWeight > maxWeight) {
                continue;
            }

            // 5) Local search again
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

                    // textbook local search: flip one bit
                    neighbour[i] = 1 - neighbour[i];

                    float weight = 0;
                    float value = 0;
                    for (int j = 0; j < n; j++) {
                        if (neighbour[j] == 1) {
                            weight += instances.get(j).wheight;
                            value += instances.get(j).value;
                        }
                    }

                    // reject overweight neighbour
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

                    candidateWeight = 0;
                    candidateValue = 0;
                    for (int i = 0; i < n; i++) {
                        if (candidate[i] == 1) {
                            candidateWeight += instances.get(i).wheight;
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

            // 6) Acceptance criteria
            if (candidateValue >= currentValue) {
                for (int i = 0; i < n; i++) {
                    current[i] = candidate[i];
                }
            }

            if (candidateValue > bestFitness) {
                bestFitness = candidateValue;
                bestWeight = candidateWeight;
                for (int i = 0; i < n; i++) {
                    best[i] = candidate[i];
                }
            }
        }

        long endTime = System.nanoTime();
        double runtimeSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Seed: " + seed);
        System.out.print("ILS Solution: ");
        for (int i = 0; i < best.length; i++) {
            System.out.print(best[i]);
        }
        System.out.println();
        System.out.println("ILS Total value: " + bestFitness);
        System.out.println("ILS Runtime (seconds): " + runtimeSeconds);
        System.out.println() ;
    }
    public static void main(String[] args) {
        LoadOptimums();
        optimums.forEach((filename, opt) -> {
            System.out.println("filename: " + filename );
            System.out.println( "Known optimum: " + opt);
            System.out.println( "Knapsack results after Iterated Local Search:");
            List<Item> instances = ReadInstance(filename);
            ILS(instances);
        });
    }

}

