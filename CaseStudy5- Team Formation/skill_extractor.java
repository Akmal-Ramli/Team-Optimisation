/////////////////////////////////////////////////////////////////////////////////////////////////////////
// TEAM FORMING PROBLEM
// Developer: Prof Kamal Z. Zamli
// Updated By: Muhammad Akmaluddin Bin Ahmad Ramli, Degree student
// Usage: java skill_extractor -i FileName -o DataFile 
//        java skill_extractor -i FileName 
// Usage Example: java skill_extractor -i Staff_Expertise_DataSet.txt -o ExtractedSkill_Staff_Expertise_Data.txt
// FileName format e.g: kamalz@ump.edu.my = Software Engineering, Optimization, Artificial Intelligence
// Make sure end of FileName has no more empty line
// Remove duplicate authors and their skills
// Remove duplicate skills
/////////////////////////////////////////////////////////////////////////////////////////////////////////


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class skill_extractor
{
    public static String input_file = "";
    public static String output_file = "";
    public static boolean create_data_file = false;
    static ArrayList<String> skills_list = new ArrayList<String>();
    static ArrayList<String> persons_list = new ArrayList<String>();
    static ArrayList<String> contents_list = new ArrayList<String>();

    static int[][] connections;
    static int size = 0;
    static int skill_count = 0;
    static int person_count = 0;

    ///////////////////////////////////////////////////////////
    //     Main Program
    ////////////////////////////////////////////////////////////
    public static void main(String[] args) throws IOException
    {        
        process_cmd_line(args);
        System.out.println("### TEAM FORMATION - SKILLS EXTRACTOR ###");
        System.out.println("Input File = " + input_file);
        if (create_data_file)
        {
            System.out.println("Output File = " + output_file);
        }

        System.out.println("Loading skills in arraylist...");
        load_file_in_arraylist(input_file);

        System.out.println("Creating skill metric...");
        main_processing();

        if (create_data_file)
        {
            System.out.println("Writing skill metric for all experts to file...");
            display_metric_to_file();
        }
        else
        {
            System.out.println("Displaying skill metric for all experts");
            display_verbose_metric();
        }
    }

    public static void process_cmd_line(String[] args) throws IOException
    {
        // Process argument lists  

        if (args.length == 0)
        {
            System.out.println("Missing skills file");
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
        int nn = 1;

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
                if (!persons_list.contains(tmp[0].trim().toLowerCase()))
                {
                    person_count++;
//                    System.out.println("Person Count = " + "[" + person_count + "] Registering New Person --> " + tmp[0]);
                    persons_list.add(tmp[0].trim().toLowerCase());

                    // get the person's skills		 
//                    System.out.println(nn++ +": "+tmp[1]);
                    String[] skills = tmp[1].split(",");
                    for (int i = 0; i < skills.length; i++)
                    {
                        if (!skills_list.contains(skills[i].trim().toLowerCase()))
                        {
                            skill_count++;
                            //System.out.println("Skill Count = " + "[" + skill_count + "] Registering New Skills --> " + skills[i]);
                            System.out.println(skills[i]);
                            skills_list.add(skills[i].trim().toLowerCase());
                        }
                        else
                        {
                            //System.out.println("Skipped. Skills Already Defined!");
                        }
                    }
                }
                else // invalid and duplicates
                {
                    //System.out.println("Duplicate Person Found -->" + tmp[0]);
                    contents_list.remove(contents_list.size() - 1);
                }
            }
        }
        size = person_count;
        System.out.println("Total Experts: " + persons_list.size());
        System.out.println("Unique Skills: " + skills_list.size());
        f.close();
    }

    public static void main_processing()
    {

        connections = new int[size][skills_list.size()];

        for (int i = 0; i < contents_list.size(); i++)
        {
            String s = contents_list.get(i).trim().toLowerCase().replaceAll("\\s+", " ");
            String[] tmp = s.split("=");
            // get the person's skills		 
            String[] skills = tmp[1].split(",");

            //System.out.println("ID Count = [" + i + "] Mapping of Skills Matrix For -> " + persons_list.get(i));
            //Create skill metric
            for (String str : skills)
            {

                if (skills_list.contains(str.trim().toLowerCase()))
                {
                    int sk = skills_list.indexOf(str.trim().toLowerCase());
                    //System.out.println("[" + persons_list.get(i) + "] Skill Index: " + sk);
                    connections[i][sk] = 1;
                }

            }
        }

    }

    ////////////////////////////////////////////////////////////
    //     DISPLAY SKILLS VERBOSE METRICS
    /////////////////////////////////////////////////////////////
    public static void display_verbose_metric()
    {
        System.out.print("\n\nPersons Mapping = ");
        for (int i = 0; i < persons_list.size(); i++)
        {
            if (i < persons_list.size() - 1)
            {
                System.out.print("[" + i + "]" + persons_list.get(i).trim() + ",");
            }
            else
            {
                System.out.print("[" + i + "]" + persons_list.get(i).trim());
            }
        }

        System.out.println();

        System.out.print("\nSkills Mapping = ");
        for (int i = 0; i < skills_list.size(); i++)
        {
            if (i < skills_list.size() - 1)
            {
                System.out.print("[" + i + "]" + skills_list.get(i).trim() + ",");
            }
            else
            {
                System.out.print("[" + i + "]" + skills_list.get(i).trim());
            }
        }
        System.out.println("\n");
        System.out.println("Display skill metric for all experts.");
        for (int row = 0; row < persons_list.size(); row++)
        {
            System.out.print("  [" + row + "] " + persons_list.get(row).trim() + " \t= ");
            for (int col = 0; col < skills_list.size(); col++)
            {
                if (col < skills_list.size() - 1)
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
    //     DISPLAY SKILLS METRICS To FILE
    /////////////////////////////////////////////////////////////
    public static void display_metric_to_file() throws FileNotFoundException
    {
        PrintWriter outfile = new PrintWriter(output_file);

        outfile.print("Total Persons =" + persons_list.size() + "\n");
        outfile.print("Total Skills =" + skills_list.size() + "\n");

        outfile.print("Persons Mapping =");

        for (int i = 0; i < persons_list.size(); i++)
        {
            if (i < persons_list.size() - 1)
            {
                outfile.print(persons_list.get(i).trim() + ",");
            }
            else
            {
                outfile.print(persons_list.get(i).trim());
            }
        }
        outfile.print("\n");

        outfile.print("Skills Mapping =");
        for (int i = 0; i < skills_list.size(); i++)
        {
            if (i < skills_list.size() - 1)
            {
                outfile.print(skills_list.get(i).trim() + ",");
            }
            else
            {
                outfile.print(skills_list.get(i).trim());
            }
        }
        outfile.print("\n");

        for (int row = 0; row < persons_list.size(); row++)
        {
            outfile.print(persons_list.get(row) + "\t=");

            for (int col = 0; col < skills_list.size(); col++)
            {

                if (col < skills_list.size() - 1)
                {
                    outfile.print(connections[row][col] + ":");
                }
                else
                {
                    outfile.print(connections[row][col]);
                }
            }
            if (row < persons_list.size() - 1)
            {
                outfile.print("\n");
            }
        }
        outfile.close();
    }
}
