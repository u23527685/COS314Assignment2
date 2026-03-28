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
    private static void ILS(){}

    //Iterated Local Search Helpers

    public static void main(String[] args) {
        LoadOptimums();
        optimums.forEach((filename, opt) ->{
            System.out.println("filename: " + filename + " , optimum: " + opt);
            List<Item>instances=ReadInstance(filename);
            for(Item instance : instances)
                instance.print();
                }
        );

    }
}
