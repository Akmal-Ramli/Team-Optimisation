/////////////////////////////////////////////////////////////////////////////////////////////////////////
// TEAM FORMING PROBLEM
// Original Developer: Prof Kamal Z. Zamli
// Updated By: Muhammad Akmaluddin Bin Ahmad Ramli, Degree student
// Usage: java cost_extractor -i FileName -o DataFile
//        java cost_extractor -i FileName
// Usage Example: java cost_extractor -i Staff_Expertise_DataSet.txt -o ConnectionCost_Staff_Expertise_Data.txt
// FileName format e.g: kamalz@ump.edu.my = Software Engineering, Optimization, Artificial Intelligence
// Make sure end of FileName has no more empty line
/////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class cost_extractor
{

    public static String input_file = "C:\\Users\\USER\\OneDrive\\Documents\\Msc\\Abdullah Al Daimi\\CaseStudy5- Team Formation\\Staff_Expertise_DataSet.txt";
    public static String output_file = "";
    public static boolean create_data_file = false;
    static double[][] connections;
    static int size = 0;
    static ArrayList<String> contents_list = new ArrayList<String>();
    static ArrayList<String> persons_lookup = new ArrayList<String>(); // mapping of persons

    ///////////////////////////////////////////////////////////
    //     Main Program
    ////////////////////////////////////////////////////////////
    public static void main(String[] args) throws IOException
    {
    	System.out.println("Input____File = " + input_file);
        process_cmd_line(args);
        System.out.println("### TEAM FORMING - COST EXTRACTOR ###");
        System.out.println("Input File = " + input_file);
        if (create_data_file)
        {
            System.out.println("Output File = " + output_file);
        }
        System.out.println("Loading file in arraylist...");
        load_file_in_arraylist(input_file);

        System.out.println("Processing contents started...");
        main_processing_contents();
        System.out.println("Processing contents end");
        if (create_data_file)
        {
            System.out.println("Writing connection cost to file");
            display_metric_to_file();
        }
        else
        {
            System.out.println("Displaying person lookup and verbose metric..");
            display_person_lookup();
            display_verbose_metric();
        }
    }

    public static void process_cmd_line(String[] args) throws IOException
    {

        // Process argument lists  
        if (args.length == 0)
        {
            System.out.println("Missing Expert-Skills File.");
            System.exit(0);
        }
        else
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-i"))
                {
                    if (i + 1 < args.length)
                    {
                        i++;
                        input_file = args[i];
                    }

                }
                else if (args[i].equals("-o"))
                {
                    if (i + 1 < args.length)
                    {
                        i++;
                        output_file = args[i];
                    }
                    create_data_file = true;
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    public static void load_file_in_arraylist(String input_file) throws IOException
    {

        ///// Access input files
        RandomAccessFile f = new RandomAccessFile(input_file, "rw");
        long length = f.length();
        long position = 0;
        int i = 1;
        // rewind file to position 0
        f.seek(0);
        while (position < length)
        {
            String content = f.readLine().trim().toLowerCase().replaceAll("\\s+", " ");
            position = f.getFilePointer();
            if (!content.isEmpty())
            {
                contents_list.add(content);

                String[] tmp = content.split("=");

                // get the person's identity                        
                if (!persons_lookup.contains(tmp[0].trim().toLowerCase()))
                {
                    persons_lookup.add(tmp[0].trim().toLowerCase().replaceAll("\\s+", " "));
                    System.out.println("Index = [" + i++ + "] Processing for --> " + tmp[0]);
                }
                else
                {
                    contents_list.remove(contents_list.size() - 1);
                }
            }
        }
        f.close();
        size = contents_list.size();
        System.out.println("Total Lines in Contents: " + contents_list.size());
        System.out.println("Number of Experts: " + persons_lookup.size());
    }

    public static void main_processing_contents()
    {

        connections = new double[size][size];
        //person_lookup = new String[size];
        for (int i = 0; i < size; i++)
        {
            String s1 = contents_list.get(i);

            // pairwise comparison with itself 
            for (int j = i + 1; j < contents_list.size(); j++)
            {
                String s2 = contents_list.get(j);
                weight_processing(i, j, s1, s2);
            }
        }
    }

    public static void weight_processing(int idx1, int idx2, String content1, String content2)
    {

        // Separate expert and skills
        String[] tmp1 = content1.split("=");
        String[] tmp2 = content2.split("=");

        // Get the person's skills
        String[] tmp3 = tmp1[1].split(",");
        String[] tmp4 = tmp2[1].split(",");

        ArrayList<String> tmp3_list = new ArrayList<>();
        ArrayList<String> tmp4_list = new ArrayList<>();
        ArrayList<String> common = new ArrayList<>();

        for (int i = 0; i < tmp3.length; i++)
        {
            if (!tmp3_list.contains(tmp3[i].trim().toLowerCase().replaceAll("\\s+", " ")))
            {
                tmp3_list.add(tmp3[i].trim().toLowerCase().replaceAll("\\s+", " "));
            }
        }

        for (int i = 0; i < tmp4.length; i++)
        {
            if (!tmp4_list.contains(tmp4[i].trim().toLowerCase().replaceAll("\\s+", " ")))
            {
                tmp4_list.add(tmp4[i].trim().toLowerCase().replaceAll("\\s+", " "));
            }
        }
        common.addAll(tmp3_list);
        common.retainAll(tmp4_list);

        int intersection = common.size();
        int union = tmp3_list.size() + tmp4_list.size() - intersection;
        double communication_cost = 1.0 - ((double) intersection / (double) union);
        connections[idx1][idx2] = communication_cost;
        connections[idx2][idx1] = communication_cost;
        //System.out.println("idx1: "+idx1+", idx:2: "+idx2+", intersection: "+intersection+", Union: " +union+ ", C Cost: "+communication_cost);
    }

    public static void display_person_lookup()
    {
        System.out.println("\nNo of Persons = " + size);
        System.out.print("List of Persons = ");
        for (int i = 0; i < size; i++)
        {
            if (i < size - 1)
            {
                System.out.print(persons_lookup.get(i).trim() + ",");
            }
            else
            {
                System.out.print(persons_lookup.get(i).trim());
            }
        }
        System.out.println();
    }

    ////////////////////////////////////////////////////////////
    //     DISPLAY COST VERBOSE METRICS
    /////////////////////////////////////////////////////////////
    public static void display_verbose_metric()
    {
        System.out.print("\n\nPersons Mapping = ");
        for (int i = 0; i < size; i++)
        {
            if (i < size - 1)
            {
                System.out.print("[" + i + "]" + persons_lookup.get(i).trim() + ",");
            }
            else
            {
                System.out.print("[" + i + "]" + persons_lookup.get(i).trim());
            }
        }
        System.out.println();

        System.out.println("\n### Connection Weight ###");
        for (int row = 0; row < size; row++)
        {
            System.out.print("  [" + row + "] " + persons_lookup.get(row) + " \t= ");
            for (int col = 0; col < size; col++)
            {
                if (col < size - 1)
                {
                    System.out.print(connections[row][col] + ":");
                }
                else
                {
                    System.out.println(connections[row][col]);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////
    //     DISPLAY COST METRICS TO FILE
    /////////////////////////////////////////////////////////////
    public static void display_metric_to_file() throws FileNotFoundException
    {
        PrintWriter outfile = new PrintWriter(output_file);
        //System.out.print ("Person Mapping = ");
        outfile.print("Persons Mapping =");
        for (int i = 0; i < size; i++)
        {
            if (i < size - 1)
            {
                outfile.print(persons_lookup.get(i).trim() + ", ");
            }
            else
            {
                outfile.print(persons_lookup.get(i).trim());
            }
        }
        outfile.print("\n");

        for (int row = 0; row < size; row++)
        {
            outfile.print(persons_lookup.get(row) + "\t= ");

            for (int col = 0; col < size; col++)
            {
                if (col < size - 1)
                {
                    outfile.print(connections[row][col] + ":");
                }
                else
                {
                    outfile.print(connections[row][col]);
                }
            }
            if (row < size - 1)
            {
                outfile.print("\n");
            }
        }
        outfile.close();
    }
}
