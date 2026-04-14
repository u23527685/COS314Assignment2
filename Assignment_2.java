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
    private static void Genetics(List<Item> instances){
        //Initializing population
        int seed= instances.size();
        float maxWheight= instances.get(0).wheight;
        Random rand = new Random(seed);
        int populationSize=seed*10;
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
        int crossOverPoint=seed/2;

        while(iteration <=10){
            // Find best in population
            for(int i=0;i<populationSize;i++){
                if(population[i]==null)
                    continue;
                if(population[i][0]==null)
                    continue;
                float currFitness=Fitness(population[i],maxWheight,instances);
                if(currFitness>= bestFitness && currFitness>=0) {
                    bestFitness = currFitness;
                    best=population[i];
                }
            }

            // --- Crossover: produce a new population from pairs of parents ---
            Integer[][] newPopulation = new Integer[populationSize*2][seed];
            int childIndex = 0;


            // Tournament
            for (int i = 0; i < populationSize; i += 2) {

                // Skip if either competing individual is null
                if (population[i] == null || population[(i + 1) % populationSize] == null)
                    continue;

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
                    if (j < crossOverPoint) {
                        child1[j] = parentA[j];
                        child2[j] = parentB[j];
                    } else {
                        child1[j] = parentB[j];
                        child2[j] = parentA[j];
                    }
                }

                // --- Mutation ---
                float mutationRate = 0.05f;
                for (int j = 0; j < seed; j++) {
                    if (rand.nextFloat() < mutationRate)
                        child1[j] = 1 - child1[j];
                    if (rand.nextFloat() < mutationRate)
                        child2[j] = 1 - child2[j];
                }

                // Place children into the new population
                if (childIndex + 1 < newPopulation.length) {
                    newPopulation[childIndex]     = child1;
                    newPopulation[childIndex + 1] = child2;
                    childIndex += 2;
                }
            }

            // Replace old population with the new doubled population
            population = newPopulation;
            populationSize = childIndex;

            iteration++;
        }

        if(bestFitness<0)
            System.out.println("No solution found");
        else {
            System.out.print(" Solution: ");
            for(int i=0; i<best.length;i++)
                System.out.print(best[i]);
            System.out.println(" Total value: " + (bestFitness));
        }
    }

    // Genetics Helpers
    private static float Fitness(Integer[] bits,float maxWheight,List<Item> instances){
        float wheight=0;
        float value=0;
        for(int i=0;i< bits.length;i++){
            if(bits[i]==1) {
                wheight += instances.get(i).wheight;
                value += instances.get(i).value;
            }
        }
        if(wheight>maxWheight)
            return -1;
        else
            return value;
    }


    //Iterated Local Search
    private static void ILS(){}

    //Iterated Local Search Helpers

    public static void main(String[] args) {
        LoadOptimums();
        optimums.forEach((filename, opt) ->{
            System.out.println("filename: " + filename + " , Known optimum: " + opt);
            List<Item>instances=ReadInstance(filename);
            Genetics(instances);
        }
        );

    }
}
