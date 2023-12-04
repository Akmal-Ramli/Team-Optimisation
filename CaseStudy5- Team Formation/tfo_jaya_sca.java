// Team Forming Optimization Using Jaya and Sine-Cosine Algorithm
// Author: Prof Kamal Z. Zamli
// Updated By: Md. Abdul Kader
// Updated by: Muhammad Akmaluddin Bin Ahmad Ramli
// v =  file to define skills to find
// c = file output from CostExtractor
// s = file output from SkillExtractor
// Usage: java tfo_jaya_sca -v SkillToFindFileName -c ExtractedConnectionCostFileName -s ExtractedSkillFileName
// Usage Example: java tfo_jaya_sca -v stf.txt -c ConnectionCost_Staff_Expertise_Data.txt -s ExtractedSkill_Staff_Expertise_Data.txt


import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class tfo_jaya_sca
{
    static String costs_file = "";
    static String skills_file = "";
    static String values_file = "";

    static ArrayList<String> skills_list = new ArrayList<String>();
    static ArrayList<String> persons_list = new ArrayList<String>();
    static ArrayList<String> unprocess_skills_connections = new ArrayList<String>();
    static ArrayList<String> unprocess_costs_connections = new ArrayList<String>();
    static ArrayList<String> memory_list = new ArrayList<String>();
    static int total_persons;
    static int total_skills;
    static double[][] costs_connections;
    static int[][] skills_connections;
    static int[] skills_to_find;
    static int max_fitness_evaluation = 10;
    static int count_fitness_evaluation = 0;
    static int max_iteration = 5;

    // Original Population Array
    static int[][] org_population;
    static String[] org_population_short_seq_list;
    static int[] org_obj_value1_array;
    static double[] org_obj_value2_array;
    // Copy of Original Population Array
    static int[][] population;
    static String[] population_short_seq_list;
    static int[] obj_value1_array;
    static double[] obj_value2_array;
    static int population_size = 10;// number of test sequence


    public static void main(String[] args) throws IOException
    {
        System.out.println("Team formation using Jaya, Sine-Cosine, Firefly, PSO Algorithm");
        
        process_cmd_line(args);
        System.out.println("#Loading Skills File and Processing : Started");
        load_skills_in_memory(skills_file);
        process_skills();
        initialize_skills_connections();
        System.out.println("#Loading Skills File and Processing : Completed");
        System.out.println("#Loading Costs File and Processing : Started");
        load_costs_in_memory(costs_file);
        process_costs();
        initialize_costs_connections();
        System.out.println("#Loading Costs File and Processing : Completed");
        System.out.println("#Loading Skills Those Required to Find Best Team : Started");
        load_skills_to_find(values_file);
        System.out.println("#Loading Skills Those Required to Find Best Team : Completed");  
        
        initialize_population();

        tfo_jaya();
        search_sine_cosine();
        tfo_firefly();
        tfo_pso();
        
        exit(0);
    }

    
    //////////////////////////////////////////////////////////////
    //   TFO using Jaya Algorithm  
    //////////////////////////////////////////////////////////////
    public static void tfo_jaya()
    {
        System.out.println("\n####TFO Using Jaya started...\n");

        int loop = 0;
        int seq[];
        int current_seq[];
        int pBest_long_seq[];
        int updated_long_seq[] = new int[total_persons];
        String updated_short_seq_string;
        String current_short_seq_string;
        int updated_obj1;
        double updated_obj2;
        int current_obj1;
        double current_obj2;

        initialize_population_copy(); // copy and use same population for each algorithm
        Random random = new Random();
        while (loop < max_iteration)
        {
            for (int i = 0; i < population_size; i++)
            {
                // current population
                current_seq = get_population_long_sequence(i);
                current_short_seq_string = population_short_seq_list[i];
                current_obj1 = obj_value1_array[i];
                current_obj2 = obj_value2_array[i];
                // System.out.println("Current seq => " + array_sequence_to_string(current_seq));
                // System.out.println("Current short seq => " + current_short_seq_string);
                // System.out.println("No of person [current] = " + current_obj1);
                // System.out.println("Costs [current] = " + current_obj2);

                int idx_best = get_index_best_cost();
                int best_long_seq[] = get_population_long_sequence(idx_best);

                int idx_worst = get_index_worst_cost();
                int worst_long_seq[] = get_population_long_sequence(idx_worst);

                for (int j = 0; j < current_seq.length; j++)
                {
                    updated_long_seq[j] = current_seq[j] + (int) (random.nextDouble() * (best_long_seq[j] - current_seq[j]))
                            - (int) (random.nextDouble() * (worst_long_seq[j] - current_seq[j]));
                }

                // must ensure legally correct sequence
                updated_long_seq = ensure_legal_sequence(updated_long_seq);
                seq = objective_function_(updated_long_seq);
                updated_short_seq_string = array_sequence_to_string(seq);
                updated_obj1 = objective_value1_(seq);
                updated_obj2 = objective_value2_(seq);
                //System.out.println("Updated current seq => " + array_sequence_to_string(updated_long_seq));
                //System.out.println("Updated short seq => " + updated_short_seq_string);
                //System.out.println("No of person [updated] = " + updated_obj1);
                //System.out.println("Costs [updated] = " + updated_obj2);

                // if the cost is improved (i.e. much less)  
                // replace the current population 
                if (updated_obj2 < current_obj2)
                {
                    //System.out.println("Best Seq updated (i.e. updated<current)");

                    // update current population long sequence
                    for (int col = 0; col < total_persons; col++)
                    {
                            population[i][col] = updated_long_seq[col];
                    }

                    // update current population short sequence
                    population_short_seq_list[i] = updated_short_seq_string;

                    // update objective values
                    obj_value1_array[i] = updated_obj1;
                    obj_value2_array[i] = updated_obj2;
                }

            }
            loop++;
        }

        int idx = get_index_best_cost();
        String best_seq_string = population_short_seq_list[idx];
        int no_of_person = obj_value1_array[idx];
        double best_cost = obj_value2_array[idx];

        System.out.println("--------------------------------------------");
        System.out.println("Final Best Sequence ==> " + best_seq_string);
        System.out.println(" [ Team Members ] " + remap_team_members(best_seq_string));
        System.out.println("Best, Maximum, Average[Member, Team Cost]: "
                + no_of_person + ", " + best_cost + ", "
                + obj_value1_array[get_index_worst_cost()] + ", " + obj_value2_array[get_index_worst_cost()] + ", "
                + measure_mean_teammembers_() + ", " + measure_mean_teamcost_());
    }

    //////////////////////////////////////////////////////////////
    //   Sine Cosine Algorithm
    //////////////////////////////////////////////////////////////	
    public static void search_sine_cosine()
    {
        System.out.println("\n####TFO Using SC started...\n");
        
        int loop = 0;
        int seq[];
        int current_seq[];
        int pBest_long_seq[];
        int idx_best;
        int idx_2nd_best;
        int updated_long_seq[] = new int[total_persons];
        String updated_short_seq_string;
        String current_short_seq_string;
        int updated_obj1;
        double updated_obj2;
        int current_obj1;
        double current_obj2;
        double Amplitude = total_persons;
        double r1;

        initialize_population_copy(); // copy and use same population for each algorithm
        Random random = new Random();
        while (loop < max_iteration)
        {
            r1 = Amplitude * (1 - (loop / max_iteration));
            for (int i = 0; i < population_size; i++)
            {
                // current population
                current_seq = get_population_long_sequence(i);
                current_short_seq_string = population_short_seq_list[i];
                current_obj1 = obj_value1_array[i];
                current_obj2 = obj_value2_array[i];
                // System.out.println("Current seq => " + array_sequence_to_string(current_seq));
                // System.out.println("Current short seq => " + current_short_seq_string);
                // System.out.println("No of person [current] = " + current_obj1);
                // System.out.println("Costs [current] = " + current_obj2);

                idx_best = get_index_best_cost();
                int best_long_seq[] = get_population_long_sequence(idx_best);
                if (random.nextDouble() < 0.5)
                {
                    for (int j = 0; j < current_seq.length; j++)
                    {
                        double r2 = 180 * random.nextDouble() / 3.14;
                        double r3 = 180 * random.nextDouble() / 3.14;
                        updated_long_seq[j] = current_seq[j] + (int) (r1 * Math.sin(r2) * ((r3 * best_long_seq[j]) - current_seq[j]));
                    }
                }
                else
                {
                    for (int j = 0; j < current_seq.length; j++)
                    {
                        double r2 = 180 * random.nextDouble() / 3.14;
                        double r3 = 180 * random.nextDouble() / 3.14;
                        updated_long_seq[j] = current_seq[j] + (int) (r1 * Math.cos(r2) * ((r3 * best_long_seq[j]) - current_seq[j]));
                    }

                }

                // must ensure legally correct sequence
                updated_long_seq = ensure_legal_sequence(updated_long_seq);
                seq = objective_function_(updated_long_seq);
                updated_short_seq_string = array_sequence_to_string(seq);
                updated_obj1 = objective_value1_(seq);
                updated_obj2 = objective_value2_(seq);
                //System.out.println("Updated current seq => " + array_sequence_to_string(updated_long_seq));
                //System.out.println("Updated short seq => " + updated_short_seq_string);
                //System.out.println("No of person [updated] = " + updated_obj1);
                //System.out.println("Costs [updated] = " + updated_obj2);

                // if the cost is improved (i.e. much less)  
                // replace the current population 
                if (updated_obj2 < current_obj2)
                {
                    //System.out.println("Best Seq updated (i.e. updated<current)");

                    // update current population long sequence
                    for (int col = 0; col < total_persons; col++)
                    {
                        population[i][col] = updated_long_seq[col];
                    }

                    // update current population short sequence
                    population_short_seq_list[i] = updated_short_seq_string;

                    // update objective values
                    obj_value1_array[i] = updated_obj1;
                    obj_value2_array[i] = updated_obj2;
                }

            }

            loop++;
        }

        int idx = get_index_best_cost();
        String best_seq_string = population_short_seq_list[idx];
        int no_of_person = obj_value1_array[idx];
        double best_cost = obj_value2_array[idx];

        System.out.println("--------------------------------------------");
        System.out.println("Final Best Sequence ==> " + best_seq_string);
        System.out.println(" [ Team Members ] " + remap_team_members(best_seq_string));
        System.out.println("Best, Maximum, Average[Member, Team Cost]: "
                + no_of_person + ", " + best_cost + ", "
                + obj_value1_array[get_index_worst_cost()] + ", " + obj_value2_array[get_index_worst_cost()] + ", "
                + measure_mean_teammembers_() + ", " + measure_mean_teamcost_());
        
    }

        //////////////////////////////////////////////////////////////
    //   FireFly Algorithm
    /////////////////////////////////////////////////////////////

    public static void tfo_firefly() {
    System.out.println("\n####TFO Using Firefly Algorithm started...\n");

    int loop = 0;
    int seq[];
    int current_seq[];
    int pBest_long_seq[];
    int updated_long_seq[] = new int[total_persons];
    String updated_short_seq_string;
    String current_short_seq_string;
    int updated_obj1;
    double updated_obj2;
    int current_obj1;
    double current_obj2;

    initialize_population_copy(); // copy and use the same population for each algorithm
    Random random = new Random();

    // Parameters for Firefly Algorithm
    double alpha = 0.5; // Randomness factor
    double beta0 = 1.0; // Attractiveness base
    double gamma = 1.0; // Absorption coefficient

    while (loop < max_iteration) {
        for (int i = 0; i < population_size; i++) {
            // Current population
            current_seq = get_population_long_sequence(i);
            current_short_seq_string = population_short_seq_list[i];
            current_obj1 = obj_value1_array[i];
            current_obj2 = obj_value2_array[i];
            // System.out.println("Current seq => " + array_sequence_to_string(current_seq));
            // System.out.println("Current short seq => " + current_short_seq_string);
            // System.out.println("No of person [current] = " + current_obj1);
            // System.out.println("Costs [current] = " + current_obj2);

            int idx_best = get_index_best_cost();
            int best_long_seq[] = get_population_long_sequence(idx_best);

            for (int j = 0; j < current_seq.length; j++) {
                double beta = beta0 * Math.exp(-gamma * distance(current_seq, best_long_seq));
                updated_long_seq[j] = (int) (current_seq[j] + beta * (random.nextDouble() - 0.5));
            }

            // Must ensure legally correct sequence
            updated_long_seq = ensure_legal_sequence(updated_long_seq);
            seq = objective_function_(updated_long_seq);
            updated_short_seq_string = array_sequence_to_string(seq);
            updated_obj1 = objective_value1_(seq);
            updated_obj2 = objective_value2_(seq);

            // If the cost is improved (i.e. much less)  
            // Replace the current population 
            if (updated_obj2 < current_obj2) {
                // Update current population long sequence
                for (int col = 0; col < total_persons; col++) {
                    population[i][col] = updated_long_seq[col];
                }

                // Update current population short sequence
                population_short_seq_list[i] = updated_short_seq_string;

                // Update objective values
                obj_value1_array[i] = updated_obj1;
                obj_value2_array[i] = updated_obj2;
            }
        }
        loop++;
    }

    int idx = get_index_best_cost();
    String best_seq_string = population_short_seq_list[idx];
    int no_of_person = obj_value1_array[idx];
    double best_cost = obj_value2_array[idx];

    System.out.println("--------------------------------------------");
    System.out.println("Final Best Sequence ==> " + best_seq_string);
    System.out.println(" [ Team Members ] " + remap_team_members(best_seq_string));
    System.out.println("Best, Maximum, Average[Member, Team Cost]: "
            + no_of_person + ", " + best_cost + ", "
            + obj_value1_array[get_index_worst_cost()] + ", " + obj_value2_array[get_index_worst_cost()] + ", "
            + measure_mean_teammembers_() + ", " + measure_mean_teamcost_());
}

private static double distance(int[] seq1, int[] seq2) {
    double distance = 0;
    for (int i = 0; i < seq1.length; i++) {
        distance += Math.pow(seq1[i] - seq2[i], 2);
    }
    return Math.sqrt(distance);
}

        //////////////////////////////////////////////////////////////
    //   Particle Swarm Optimisation Algorithm
    /////////////////////////////////////////////////////////////
    public static void tfo_pso() {
        System.out.println("\n####TFO Using PSO started...\n");
    
        int loop = 0;
        int seq[];
        int current_seq[];
        int pBest_long_seq[];
        int updated_long_seq[] = new int[total_persons];
        String updated_short_seq_string;
        String current_short_seq_string;
        int updated_obj1;
        double updated_obj2;
        int current_obj1;
        double current_obj2;
    
        initialize_population_copy(); // copy and use the same population for each algorithm
        Random random = new Random();
    
        // PSO-specific parameters
        double c1 = 2.0;
        double c2 = 2.0;
        double inertiaWeight = 0.9;
        double maxVelocity = 2.0;
        double[] pBestValue = new double[population_size];
        double[] gBestValue = new double[population_size];
        int[][] pBestPosition = new int[population_size][total_persons];
        int[] gBestPosition = new int[total_persons];
    
        // Initialization of pBest and gBest
        for (int i = 0; i < population_size; i++) {
            pBestValue[i] = Double.MAX_VALUE;
            pBestPosition[i] = get_population_long_sequence(i);
        }
        gBestValue[0] = Double.MAX_VALUE;
        gBestPosition = get_population_long_sequence(get_index_best_cost());
    
        while (loop < max_iteration) {
            for (int i = 0; i < population_size; i++) {
                current_seq = get_population_long_sequence(i);
                current_short_seq_string = population_short_seq_list[i];
                current_obj1 = obj_value1_array[i];
                current_obj2 = obj_value2_array[i];
    
                int idx_best = get_index_best_cost();
                int best_long_seq[] = get_population_long_sequence(idx_best);
    
                // Update velocity and position
                for (int j = 0; j < current_seq.length; j++) {
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();
    
                    double velocity = inertiaWeight * current_seq[j] + c1 * r1 * (pBestPosition[i][j] - current_seq[j]) + c2 * r2 * (gBestPosition[j] - current_seq[j]);
    
                    if (velocity > maxVelocity) {
                        velocity = maxVelocity;
                    } else if (velocity < -maxVelocity) {
                        velocity = -maxVelocity;
                    }
    
                    updated_long_seq[j] = current_seq[j] + (int) velocity;
                }
    
                updated_long_seq = ensure_legal_sequence(updated_long_seq);
                seq = objective_function_(updated_long_seq);
                updated_short_seq_string = array_sequence_to_string(seq);
                updated_obj1 = objective_value1_(seq);
                updated_obj2 = objective_value2_(seq);
    
                if (updated_obj2 < pBestValue[i]) {
                    pBestValue[i] = updated_obj2;
                    pBestPosition[i] = updated_long_seq;
                }
    
                if (updated_obj2 < gBestValue[0]) {
                    gBestValue[0] = updated_obj2;
                    gBestPosition = updated_long_seq;
                }
    
                if (updated_obj2 < current_obj2) {
                    for (int col = 0; col < total_persons; col++) {
                        population[i][col] = updated_long_seq[col];
                    }
    
                    population_short_seq_list[i] = updated_short_seq_string;
    
                    obj_value1_array[i] = updated_obj1;
                    obj_value2_array[i] = updated_obj2;
                }
            }
            loop++;
        }
    
        int idx = get_index_best_cost();
        String best_seq_string = population_short_seq_list[idx];
        int no_of_person = obj_value1_array[idx];
        double best_cost = obj_value2_array[idx];
    
        System.out.println("--------------------------------------------");
        System.out.println("Final Best Sequence ==> " + best_seq_string);
        System.out.println(" [ Team Members ] " + remap_team_members(best_seq_string));
        System.out.println("Best, Maximum, Average[Member, Team Cost]: "
                + no_of_person + ", " + best_cost + ", "
                + obj_value1_array[get_index_worst_cost()] + ", " + obj_value2_array[get_index_worst_cost()] + ", "
                + measure_mean_teammembers_() + ", " + measure_mean_teamcost_());
    }
    
    //////////////////////////////////////////////////////////////
    //   Process command line 
    /////////////////////////////////////////////////////////////
    public static void process_cmd_line(String[] args) throws IOException
    {

        // Process argument lists  
        if (args.length == 0)
        {
            System.out.println("Missing problem definition files");
            System.exit(0);
        }
        else
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-c"))
                {
                    if (i + 1 < args.length)
                    {
                        i++;
                        costs_file = args[i];
                    }

                }
                else if (args[i].equals("-s"))
                {
                    if (i + 1 < args.length)
                    {
                        i++;
                        skills_file = args[i];
                    }
                }
                else if (args[i].equals("-v"))
                {
                    if (i + 1 < args.length)
                    {
                        i++;
                        values_file = args[i];
                    }
                }

            }

        }

    }

    ////////////////////////////////////////////////////////////
    //   Load skills in memory  
    ////////////////////////////////////////////////////////////
    public static void load_skills_in_memory(String skills_file)
            throws IOException
    {
        RandomAccessFile f = new RandomAccessFile(skills_file, "rw");
        long length = f.length();
        long position = 0;
        String content;
        int count = 0;
        System.out.println("Loading skills file in memory => " + skills_file);
        // rewind file to position 0
        f.seek(0);
        memory_list.clear();
        while (position < length)
        {
            if (count % 50 == 0)
            {
                //System.out.println("Count = [" + count + "] Loading skills to memory ...please wait");
            }
            count++;
            content = f.readLine();
            memory_list.add(content);
            position = f.getFilePointer();
        }
        f.close();
    }

    ////////////////////////////////////////////////////////////
    //   Load costs in memory  
    ////////////////////////////////////////////////////////////
    public static void load_costs_in_memory(String costs_file)
            throws IOException
    {
        RandomAccessFile f = new RandomAccessFile(costs_file, "rw");
        long length = f.length();
        long position = 0;
        String content;
        int count = 0;
        System.out.println("Loading costs file in memory => " + costs_file);
        // rewind file to position 0
        f.seek(0);
        memory_list.clear();
        while (position < length)
        {
            if (count % 50 == 0)
            {
                //System.out.println("Count = [" + count + "] Loading costs to memory ...please wait");
            }
            count++;
            content = f.readLine();
            memory_list.add(content);
            position = f.getFilePointer();
        }
        f.close();
    }

    ////////////////////////////////////////////////////////////
    //   Process skills from memory 
    ////////////////////////////////////////////////////////////
    public static void process_skills()
    {
        String content;
        System.out.println("Processing skills ... ");

        for (int idx = 0; idx < memory_list.size(); idx++)
        {
            content = memory_list.get(idx);
            boolean process_already = false;
            // parse all the data for processing
            String results[] = content.split("=");
            for (int i = 0; i < results.length; i++)
            {
                String string_val = results[i];
                if (string_val.trim().equals("Total Persons".trim()))
                {
                    total_persons = Integer.parseInt(results[1]);
                    process_already = true;
                }
                else if (string_val.trim().equals("Total Skills".trim()))
                {
                    total_skills = Integer.parseInt(results[1]);
                    process_already = true;
                }
                else if (string_val.trim().equals("Persons Mapping".trim()))
                {
                    String results2[] = results[1].split(",");
                    for (int j = 0; j < results2.length; j++)
                    {
                        persons_list.add(results2[j]);
                        //System.out.println("Adding person => " + results2[j]);
                    }
                    process_already = true;
                }
                else if (string_val.trim().equals("Skills Mapping".trim()))
                {
                    String results2[] = results[1].split(",");
                    for (int j = 0; j < results2.length; j++)
                    {
                        skills_list.add(results2[j]);
                        //System.out.println("Adding skill => " + results2[j]);
                    }
                    process_already = true;
                }
                else // store unprocess skills connections 
                {

                    if (!unprocess_skills_connections.contains(content) && process_already == false)
                    {
                        unprocess_skills_connections.add(content);
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////
    //   Process costs from memory 
    ////////////////////////////////////////////////////////////
    public static void process_costs()
    {
        String content;
        System.out.println("Processing costs ... ");

        for (int idx = 0; idx < memory_list.size(); idx++)
        {
            content = memory_list.get(idx);
            boolean process_already = false;
            // parse all the data for processing
            String results[] = content.split("=");
            for (int i = 0; i < results.length; i++)
            {
                String string_val = results[i];
                if (string_val.trim().equals("Persons Mapping".trim()))
                {
                    process_already = true;
                    continue;
                }
                else // store unprocess costs connections 
                {
                    if (!unprocess_costs_connections.contains(content) && process_already == false)
                    {
                        unprocess_costs_connections.add(content);
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////
    //   Initialize skills connections 
    //////////////////////////////////////////////////////////////
    public static void initialize_skills_connections()
    {

        skills_connections = new int[total_persons][total_skills];
        // initialize initial connections
        System.out.println("Initializing skills connection metrics..");
        for (int row = 0; row < total_persons; row++)
        {
            for (int col = 0; col < total_skills; col++)
            {
                skills_connections[row][col] = 0;
            }
        }

        //    System.out.println ("Unprocess connections from skills file => "+unprocess_skills_connections.size());
        for (int i = 0; i < unprocess_skills_connections.size(); i++)
        {
            String s = unprocess_skills_connections.get(i).trim();
            String result[] = s.split("=");
            String val[] = result[1].split(":");
            for (int j = 0; j < val.length; j++)
            {
                skills_connections[i][j] = (int) Integer.parseInt(val[j]);
            }
        }

    }

    ///////////////////////////////////////////////////////////
    //   Initialize cost metric 
    ///////////////////////////////////////////////////////////
    public static void initialize_costs_connections()
    {

        costs_connections = new double[total_persons][total_persons];
        // initialize initial connections
        System.out.println("Initializing costs connection metrics..");
        for (int row = 0; row < total_persons; row++)
        {
            for (int col = 0; col < total_persons; col++)
            {
                costs_connections[row][col] = 0.0;
            }
        }

        //System.out.println ("Unprocess connections from costs file => "+unprocess_costs_connections.size());
        for (int i = 0; i < unprocess_costs_connections.size(); i++)
        {
            String s = unprocess_costs_connections.get(i).trim();
            String result[] = s.split("=");
            String val[] = result[1].split(":");
            for (int j = 0; j < val.length; j++)
            {
                costs_connections[i][j] = (double) Double.parseDouble(val[j]);
            }
        }
    }

    ////////////////////////////////////////////////////////////
    //   Load skills to find values_file as array of int 
    ////////////////////////////////////////////////////////////
    public static void load_skills_to_find(String values_file)
            throws IOException
    {
        RandomAccessFile f = new RandomAccessFile(values_file, "rw");
        long length = f.length();
        long position = 0;
        String content;
        f.seek(0);
        ArrayList<Integer> match_idx = new ArrayList<Integer>();

        while (position < length)
        {
            content = f.readLine();
            String results[] = content.split("=");
            String val[] = results[1].split(",");
            for (int i = 0; i < val.length; i++)
            {
                int index = skills_list.indexOf(val[i].trim());
                //System.out.println("Skills to search for ==> " + val[i]);
                if (index == -1)
                {
                    System.out.println("One of the skills requested does not exist...");
                    System.exit(0);
                }
                else
                {
                    match_idx.add(index);
                }
            }
            position = f.getFilePointer();
        }
        f.close();

        Collections.sort(match_idx);
        skills_to_find = new int[total_skills];
        Arrays.fill(skills_to_find, 0);

        for (int i = 0; i < total_skills; i++)
        {
            if (match_idx.contains(i))
            {
                skills_to_find[i] = 1;
            }
        }
        //System.out.println("Total Skills To Find: " + val.length());
    }

    //////////////////////////////////////////////////////////////
    //   Initialize population 
    //////////////////////////////////////////////////////////////	
    public static void initialize_population()
    {
        int arr[];
        int seq[];
        org_obj_value1_array = new int[population_size];
        org_obj_value2_array = new double[population_size];

        org_population = new int[population_size][total_persons];
        org_population_short_seq_list = new String[population_size];
        for (int row = 0; row < population_size; row++)
        {
            arr = generate_random_sequence(total_persons); // generate random sequence using whole experts
            seq = objective_function_(arr); // trim experts who doesn't have any required skills

            // long sequence - all
            //System.out.println ("Long sequence => "+array_sequence_to_string(arr));
            for (int col = 0; col < total_persons; col++)
            {
                org_population[row][col] = arr[col];
            }

            //System.out.println ("Short sequence => "+array_sequence_to_string(seq));
            // short seq - best only
            org_population_short_seq_list[row] = array_sequence_to_string(seq);

            int obj_person = objective_value1_(seq);
            double obj_costs = objective_value2_(seq);
            //System.out.println("--------------------------------------------------");
            //System.out.println("Population no = " + row);
            //System.out.println("Long sequence = " + array_sequence_to_string(arr));
            //System.out.println("Short sequence = " + array_sequence_to_string(seq));
            //System.out.println("No of person = " + obj_person);
            //System.out.println("Costs = " + obj_costs);
            org_obj_value1_array[row] = obj_person;
            org_obj_value2_array[row] = obj_costs;
        }
    }

    //////////////////////////////////////////////////////////////
    //   Copy and use same initial population in all algorithms  
    //////////////////////////////////////////////////////////////	
    public static void initialize_population_copy()
    {
        count_fitness_evaluation = 0;
        obj_value1_array = new int[population_size];
        obj_value2_array = new double[population_size];

        population = new int[population_size][total_persons];
        population_short_seq_list = new String[population_size];
        for (int row = 0; row < population_size; row++)
        {
            for (int col = 0; col < total_persons; col++)
            {
                population[row][col] = org_population[row][col];
            }

            population_short_seq_list[row] = org_population_short_seq_list[row];

            obj_value1_array[row] = org_obj_value1_array[row];
            obj_value2_array[row] = org_obj_value2_array[row];
        }
    }

    ////////////////////////////////////////////////////////////
    //   Generate random sequence 
    ////////////////////////////////////////////////////////////
    public static int[] generate_random_sequence(int dim)
    {
        int[] ar = new int[dim];
        int d, tmp;
        Random generator = new Random();

        for (int counter = 0; counter < dim; counter++)
        {
            ar[counter] = counter;
        }
        // swap ar elements with random index
        for (int i = 0; i < dim - 1; i++)
        {
            d = i + (generator.nextInt() & (dim - 1 - i));
            tmp = ar[i];
            ar[i] = ar[d];
            ar[d] = tmp;
        }
        return ar;
    }

    //////////////////////////////////////////////////////////////
    // Objective function - based on reduce sequence
    // concatenate long sequences of skills
    /////////////////////////////////////////////////////////////
    public static int[] objective_function_(int arr[])
    {
        boolean consist_skills_value = false;
        int update_answer[] = new int[total_skills];
        Arrays.fill(update_answer, 0);

        ArrayList<Integer> trim_seq = new ArrayList<>();
        String value_skills = array_sequence_to_string(skills_to_find);

        for (int i = 0; i < arr.length; i++)
        {
            consist_skills_value = false;
            if (i == 0)
            {
                for (int j = 0; j < total_skills; j++)
                {
                    update_answer[j] = skills_connections[arr[i]][j];
                }

                consist_skills_value = contain_skills_to_find(update_answer, skills_to_find);
                if (consist_skills_value)
                {
                    trim_seq.add(arr[i]);
                }
            }
            else
            {
                int tmp_answer[] = new int[total_skills];
                int common_skills[] = new int[total_skills];
                Arrays.fill(tmp_answer, 0);
                for (int j = 0; j < total_skills; j++)
                {
                    tmp_answer[j] = skills_connections[arr[i]][j];
                }

                consist_skills_value = contain_skills_to_find(tmp_answer, skills_to_find);
                if (consist_skills_value)
                {
                    int comel = 0, com;
                    
                    for (com = 0; com < skills_to_find.length; com++)
                    {
                        if (tmp_answer[com] == 1 && skills_to_find[com] == 1)
                        {
                            common_skills[comel] = com;
                            comel++;
                        }
                    }
                    for (com = 0; com < comel; com++)
                    {
                        if (update_answer[common_skills[com]] == 0)
                        {
                            trim_seq.add(arr[i]);
                            update_answer = merge_elements_(update_answer, tmp_answer);
                            break;
                        }
                    }

                    if (complete_merge_sequence_(update_answer, skills_to_find))
                    {
                        break;
                    }
                }
            }
        }

        count_fitness_evaluation++;
        int trim_result[] = new int[trim_seq.size()];
        for (int i = 0; i < trim_seq.size(); i++)
        {
            trim_result[i] = trim_seq.get(i);
        }
        return trim_result;
    }

    /////////////////////////////////////////////////////////////
    //   Convert array sequence to string
    /////////////////////////////////////////////////////////////
    public static String array_sequence_to_string(int array[])
    {
        String sequence = "";
        int length = array.length;

        for (int i = 0; i < array.length; i++)
        {
            if (i < array.length - 1)
            {
                sequence = sequence + Integer.toString(array[i]) + "-";
            }
            else
            {
                sequence = sequence + Integer.toString(array[i]);
            }
        }
        return (sequence);
    }

    //////////////////////////////////////////////////////////////
    // Calculate  objective value in terms the number of persons
    // only meaningful after objective function call
    /////////////////////////////////////////////////////////////
    public static int objective_value1_(int seq[])
    {

        return (seq.length);

    }

    //////////////////////////////////////////////////////////////
    // Calculate  objective value in terms the connection costs
    // only meaningful after objective function call
    /////////////////////////////////////////////////////////////
    public static double objective_value2_(int seq[])
    {
        double cost = 0.0;

        for (int i = 0; i < seq.length; i++)
        {
            int row = seq[i];
            for (int j = i + 1; j < seq.length; j++)
            {
                int col = seq[j];
                cost = cost + costs_connections[row][col];
            }
        }
        return (cost);
    }

    //////////////////////////////////////////////////////////
    //  Check if the given sequence has at least 1 skills 
    //  that we are looking for
    //////////////////////////////////////////////////////////
    public static boolean contain_skills_to_find(int arr[], int skills_to_find[])
    {
        boolean outcome = false;
        for (int x = 0; x < skills_to_find.length; x++)
        {
            if (skills_to_find[x] == 1 && arr[x] == 1)
            {
                outcome = true;
                break;
            }
        }
        return outcome;
    }

    ////////////////////////////////////////////////////////////
    //    Merge elements
    ///////////////////////////////////////////////////////////
    public static int[] merge_elements_(int arr1[], int arr2[])
    {

        int merge_array[] = new int[total_skills];
        Arrays.fill(merge_array, 0);

        for (int x = 0; x < arr1.length; x++)
        {

            if (arr1[x] == arr2[x])
            {
                merge_array[x] = arr1[x];
            }
            else if (arr1[x] == 0 && arr2[x] == 1)
            {
                merge_array[x] = 1;
            }
            else if (arr1[x] == 1 && arr2[x] == 0)
            {
                merge_array[x] = 1;
            }
        }
        return merge_array;
    }

    //////////////////////////////////////////////////////////
    //  Check for complete merge against skills to find
    //////////////////////////////////////////////////////////
    public static boolean complete_merge_sequence_(int arr[], int skills_to_find[])
    {
        boolean outcome = true;
        for (int x = 0; x < skills_to_find.length; x++)
        {
            if (skills_to_find[x] == 1 && arr[x] == 0)
            {
                outcome = false;
                break;
            }
        }
        return outcome;
    }

    //////////////////////////////////////////////////////////////
    //   Get the ith population long sequence
    //////////////////////////////////////////////////////////////	
    public static int[] get_population_long_sequence(int idx)
    {
        int arr[] = new int[total_persons];

        for (int col = 0; col < total_persons; col++)
        {
            arr[col] = population[idx][col];
        }
        return (arr);
    }


    ////////////////////////////////////////////////////////////
    //   Remap team members 
    ////////////////////////////////////////////////////////////
    public static String remap_team_members(String sequence)
    {
        String[] list = sequence.split("-"); //split sequence
        String team = "\n";
        for (int i = 0; i < list.length; i++)
        {
            int idx = Integer.parseInt(list[i]);
            String name = "===>" + persons_list.get(idx);
            if (i < list.length - 1)
            {
                team = team + name + "\n";
            }
            else
            {
                team = team + name;
            }

        }
        return team;
    }

    //////////////////////////////////////////////////////////////
    //   Get index for best (minimum) cost
    //////////////////////////////////////////////////////////////		
    public static int get_index_best_cost()
    {

        int idx = 0;
        double best_cost = Double.MAX_VALUE;

        for (int i = 0; i < population_size; i++)
        {
            if (obj_value2_array[i] < best_cost)
            {
                best_cost = obj_value2_array[i];
                idx = i;
            }
        }

        return idx;
    }

   
    //////////////////////////////////////////////////////////////
    //   Get index for worst (maximum) cost
    //////////////////////////////////////////////////////////////		
    public static int get_index_worst_cost()
    {

        int idx = 0;
        double worst_cost = obj_value2_array[0];

        for (int i = 1; i < population_size; i++)
        {
            if (obj_value2_array[i] > worst_cost)
            {
                worst_cost = obj_value2_array[i];
                idx = i;
            }
        }

        return idx;
    }


    //////////////////////////////////////////////////////////////
    // Ensure legal sequence within the valid range/no repetition
    //////////////////////////////////////////////////////////////
    public static int[] ensure_legal_sequence(int arr[])
    {
        int size = arr.length;
        ArrayList<Integer> temp_list = new ArrayList<Integer>();

        for (int i = 0; i < size; i++)
        {

            if (arr[i] < 0)
            {
                arr[i] = Math.abs(arr[i]);
            }

            if (arr[i] > size - 1)
            {
                arr[i] = arr[i] % size;
            }

            if (!temp_list.contains(arr[i]))
            {
                temp_list.add(arr[i]);
            }
        }

        int ref[] = generate_random_sequence(size);
        for (int i = 0; i < size; i++)
        {
            if (!temp_list.contains(ref[i]))
            {
                temp_list.add(ref[i]);
            }

            if (temp_list.size() == size)
            {
                break;
            }
        }

        int return_arr[] = new int[temp_list.size()];
        for (int i = 0; i < size; i++)
        {
            return_arr[i] = temp_list.get(i);
        }

        return return_arr;

    }

    public static double measure_mean_teamcost_()
    {
        double cost = 0;

        for (int i = 0; i < population_size; i++)
        {
            cost += obj_value2_array[i];
        }

        return cost / population_size;
    }

    public static double measure_mean_teammembers_()
    {
        double mem = 0;

        for (int i = 0; i < population_size; i++)
        {
            mem += obj_value1_array[i];
        }

        return (double) mem / population_size;
    }
}
