/***************************************************************
* file: CS4450Program2.java
* author: Justin Figueroa
* class: CS 4450 – Computer Graphics
*
* assignment: program 2
* date last modified: 09/25/2026
*
* purpose: This program draws a window and draws primitives based on coordinates
* from file passed by user via command line
*
****************************************************************/
package cs4450.program2;
import java.util.*;
import java.io.File;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.input.Keyboard;



public class CS4450Program2 {

    
//These Functions will get the user input and parse the coordinates text
public static String getPathToFile(String[] args){
    String pathToFile;
    try{
        for (int i = 0; i < args.length; i++){
        if (args[i].equals("--path") || args[i].equals("-p")){
            pathToFile = args[i + 1];
        return pathToFile;
            }
        else
            return "Invalid arguements";
        }
    }
    catch (Exception e){
        return "Usage: ./program [--path or -p /path/to/file]";
    }
    return "Usage: ./program [--path or -p /path/to/file]";
}
public static ArrayList<ArrayList<String>> parseCoordinateFile(String pathToFile){
    ArrayList<ArrayList<String>> toDraw = new ArrayList<ArrayList<String>>();
    toDraw.add(new ArrayList<String>());
    int rowArrayList = 0;
    int transformElements = 13;
    try (Scanner fileScanner = new Scanner(new File(pathToFile))) {

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();

            try (Scanner lineScanner = new Scanner(line)) {
                    
                    if (line.charAt(0) == 'P'){
                        if(rowArrayList != 0){
                        toDraw.add(new ArrayList<String>());
                        rowArrayList++;}
                        while (lineScanner.hasNext())
                        toDraw.get(rowArrayList).add(lineScanner.next());
                        
                    }
                    else if (line.charAt(0) == 'T'){
                        rowArrayList++;
                        toDraw.add(new ArrayList<String>());
                        while (lineScanner.hasNext())
                        toDraw.get(rowArrayList).add(lineScanner.next());
                    }
                    
                    else{
                        while(lineScanner.hasNext())
                        toDraw.get(rowArrayList).add(lineScanner.next());
                    }
                    
                }
            }
        }
    catch (Exception e){
        System.out.println ("File not Found");
    }
        return toDraw;        
}

public static void start(String[] args){
    try{
        String pathToFile;
        ArrayList<ArrayList<String>> toDraw;
        pathToFile = getPathToFile(args);
        toDraw = parseCoordinateFile(pathToFile);
  
        createWindow();
        Keyboard.create();
        initGL();
        render(toDraw);
    }
    catch (Exception e){
        e.printStackTrace();
    }
}

public static void end(){
    Display.destroy();
}

 private static void createWindow() throws Exception{
    Display.setFullscreen(false);
    Display.setDisplayMode(new DisplayMode(640, 480));
    Display.setTitle("Program 2: Fill + Transform");
    Display.create();
}

private static void initGL(){
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0, 640, 0, 480, 1, -1);
    glMatrixMode(GL_MODELVIEW);
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
}    

private static void render(ArrayList<ArrayList<String>> toDraw){
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
        try{
            Display.update();
            Display.sync(60);
            }
        catch (Exception e){
            e.printStackTrace();
            }
        }
    Display.destroy();
}

public static void main(String[] args) {
    start(args);
    }
}

