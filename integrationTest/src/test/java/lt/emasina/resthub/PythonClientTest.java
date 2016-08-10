package lt.emasina.resthub;

import lt.emasina.resthub.server.ServerSetup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import lombok.extern.log4j.Log4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;



@Log4j
@RunWith(JUnit4.class)
public class PythonClientTest extends ServerSetup {
        
    @Test
    public void clientTest() throws Exception {
                        
        String homeDir = System.getProperty("user.dir");
        System.out.println(homeDir);
        String python_String = null;
        String search_false = "FAILED";
        String search_true = "OK";
        String search_value = null;
        
        try {
        	   List<String> args = new ArrayList<String>();
        	   args.add ("sh"); // command name
        	   args.add ("startPython.sh");

        	   ProcessBuilder pb = new ProcessBuilder(args);

        	   pb.redirectErrorStream(true);
        	   Process proc = pb.start();
        	   
        	   Reader reader = new InputStreamReader(proc.getInputStream());
        	   BufferedReader bf = new BufferedReader(reader);
        	   String s;
        	   
        	   while ((s = bf.readLine()) != null) {
        		   System.out.println(s);
        		   python_String = python_String + s;
        	   }
        	   
        	   if (python_String.contains(search_true)) {
        		   search_value = "OK";
        	   }
        	   else if (python_String.contains(search_false)) {
        		   search_value = "FAILED";
        	   }
        	   
        	   assertEquals("OK", search_value);
        	   
        	   } 
        catch (Exception ex) {
        	   ex.printStackTrace();
        	   }       
    }

}
