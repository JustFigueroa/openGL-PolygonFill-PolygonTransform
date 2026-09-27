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
    glOrtho(-320, 320, -240, 240, 1, -1);
    glMatrixMode(GL_MODELVIEW);
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
}    

private static void render(ArrayList<ArrayList<String>> toDraw){    

    while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
        try{
        renderPolygon(toDraw);
        Display.update();
        Display.sync(60);
        }
        catch (Exception e){
            e.printStackTrace();
            }
        }
    Display.destroy();
}

private static void renderPolygon(ArrayList<ArrayList<String>> toDraw){
    ArrayList<String> polygonVertices = new ArrayList<String>();
    ArrayList<String> transformations = new ArrayList<String>();
    ArrayList<String> updatedPolygonVertices = new ArrayList<String>();

    String taskToDo = "";
    
    for (int i = 0; i < toDraw.size(); i++){

        taskToDo = toDraw.get(i).get(0);
        if (taskToDo.equals("P")){
        polygonVertices = toDraw.get(i);
        glColor3f(Float.parseFloat(polygonVertices.get(1)), Float.parseFloat(polygonVertices.get(2)), Float.parseFloat(polygonVertices.get(3)));
       }
        else if (taskToDo.equals("T")){
            transformations = toDraw.get(i);
            glPushMatrix();
            transformPolygonVertices(polygonVertices, transformations);
        glBegin(GL_LINE_LOOP);
            for (int k = 4; k+1 < polygonVertices.size(); k+=2){
                glVertex2f(Float.parseFloat(polygonVertices.get(k)), Float.parseFloat(polygonVertices.get(k+1)));
                System.out.print(String.valueOf(polygonVertices.get(k)));
                System.out.print(" ");
                System.out.print(String.valueOf(polygonVertices.get(k + 1)));
                System.out.print("\n");
            }
        glEnd();
        glPopMatrix();
        }
    }
}
private static ArrayList<String> transformPolygonVertices(ArrayList<String> polygonVertices, ArrayList<String> transformations){
        ArrayList<String> updatedPolygonVertices = new ArrayList<String>();
        ArrayList<String> thisVertice = new ArrayList<String>();
        for (int j = transformations.size()-1; j > 0; j--){
            if (transformations.get(j).equals("t")){
                translateVertices();
                glTranslatef(Float.parseFloat(transformations.get(j+1)),Float.parseFloat(transformations.get(j+2)), 0f );
                System.out.print("translate ");
                System.out.print(transformations.get(j+1));
                System.out.print(" ");
                System.out.print(transformations.get(j+2));
                System.out.print("\n");
            }
            else if (transformations.get(j).equals("r")){
                glRotatef(Float.parseFloat(transformations.get(j+1)),Float.parseFloat(transformations.get(j+2)), Float.parseFloat(transformations.get(j+3)), 1f);
                System.out.print("Rotate");
                System.out.print(transformations.get(j+1));
                System.out.print(" ");
                System.out.print(transformations.get(j+2));
                System.out.print(" ");
                System.out.print(transformations.get(j+3));
                System.out.print("\n");
            }
            else if (transformations.get(j).equals("s")){
                glTranslatef(Float.parseFloat(transformations.get(j+3)), Float.parseFloat(transformations.get(j+4)), 0f);
                glScalef(Float.parseFloat(transformations.get(j+1)), Float.parseFloat(transformations.get(j+2)), 1f);
                glTranslatef(-Float.parseFloat(transformations.get(j+3)), -Float.parseFloat(transformations.get(j+4)), 0f);
                System.out.print("Scale ");
                System.out.print(transformations.get(j+1));
                System.out.print(" ");
                System.out.print(transformations.get(j+2));
                System.out.print(" ");
                System.out.print(transformations.get(j+3));
                System.out.print(" ");
                System.out.print(transformations.get(j+4));
                System.out.print("\n");
            }
            else 
                System.out.print("Not a transformation");
        }
}
private static void rotateVertices(){
    
}
private static void scaleVertices(){
    
}
private static void translateVertices(){
    
}

private static void fillPolygon(){
    //to be implemented
}

public static void main(String[] args) {
    start(args);
    }
}

