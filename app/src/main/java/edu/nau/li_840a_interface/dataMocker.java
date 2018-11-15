package edu.nau.li_840a_interface;
import java.util.Random;

public class dataMocker {

    static final String li820_start = "<li820>";
    static final String li820_end = "</li820>";
    static final String data_start = "<data>";
    static final String data_end = "</data>";
    static final String celltemp_start = "<celltemp>";
    static final String celltemp_end = "</celltemp>";
    static final String cellpress_start = "<cellpress>";
    static final String cellpress_end = "</cellpress>";
    static final String c02_start = "<c02>";
    static final String c02_end = "</c02>";
    static final String c02abs_start = "<c02abs>";
    static final String c02abs_end = "</c02abs>";
    static final String ivolt_start = "<ivolt>";
    static final String ivolt_end = "</ivolt>";
    static final String raw_start = "<raw>";
    static final String raw_end = "</raw>";


    /* Example Licor XML
    ```<li820><data><celltemp>4.7040696e1</celltemp><cellpres>7.8869514e1</cellpres><co2>5.9971117e2</co2>
       <co2abs>8.6043262e-2</co2abs><ivolt>1.5730590e1</ivolt><raw>2360202,2642891</raw></data></li820>```
    */

    public static String generateXML(){
        String mock_xml = "";
        int counter = 0;
        Random rand = new Random();
        int  n = rand.nextInt(50) + 1; //50 is the maximum and the 1 is our minimum

        while(counter < n){
            mock_xml += li820_start + data_start + celltemp_start + "4.7040696e1" + celltemp_end + cellpress_start +
                    "7.8869514e1" + cellpress_end + c02_start + "5.9971117e2" + c02_end + c02abs_start + "8.6043262e-2" +
                    c02abs_end + ivolt_start + "1.5730590e1" + ivolt_end + raw_start + "2360202,2642891" + raw_end + data_end + li820_end;
            counter++;
        }
        return mock_xml;
    }

}
