/***************************************************************
* file: CS4450Program2.java
* author: Justin Figueroa
* class: CS 4450 – Computer Graphics
*
* assignment: program 2
* date last modified: 09/26/2026
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
    ArrayList<Float> updatedPolygonVertices = new ArrayList<Float>();

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
            updatedPolygonVertices = transformPolygonVertices(polygonVertices, transformations);
            glBegin(GL_POINTS);
            drawLines(updatedPolygonVertices);
            
        glEnd();
        glPopMatrix();
        
        }
    }
}

private static ArrayList<Float> transformPolygonVertices(ArrayList<String> polygonVertices, ArrayList<String> transformations){
        ArrayList<Float> updatedPolygonVertices = new ArrayList<Float>();
        ArrayList<String> thisTransform = new ArrayList<String>();
        ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
        ArrayList<float[][]> thisOperations = new ArrayList<float[][]>();
        float[][] compositeMatrix = new float[3][3];
        float[] newVertices = new float[3];
        float[] thisVertices = new float[3];


        for (int i = 0; i < transformations.size(); i++){
            if (transformations.get(i).equals("t")){
            thisTransform.add(transformations.get(i+1));
            thisTransform.add(transformations.get(i+2));
            matrixOperations.addAll(translateVertices(thisTransform));
            thisTransform.clear();

            }
            else if (transformations.get(i).equals("r")){
            thisTransform.add(transformations.get(i+1));
            thisTransform.add(transformations.get(i+2));
            thisTransform.add(transformations.get(i+3));
            matrixOperations.addAll(rotateVertices(thisTransform));
            thisTransform.clear();

            }
            else if (transformations.get(i).equals("s")){
            thisTransform.add(transformations.get(i+1));
            thisTransform.add(transformations.get(i+2));            
            thisTransform.add(transformations.get(i+3));
            thisTransform.add(transformations.get(i+4));
            matrixOperations.addAll(scaleVertices(thisTransform));
            thisTransform.clear();
            }
            else
                continue;
        compositeMatrix = makeCompositeMatrix(matrixOperations);
        }
        for (int i = 4; i < polygonVertices.size(); i+=2){
           thisVertices = new float[] {Float.parseFloat(polygonVertices.get(i)), Float.parseFloat(polygonVertices.get(i+1)), 1};
           newVertices = multiply(compositeMatrix, thisVertices);
           updatedPolygonVertices.add(newVertices[0]);
           updatedPolygonVertices.add(newVertices[1]);
        }
        
        return updatedPolygonVertices;
}

private static ArrayList<float[][]> translateVertices(ArrayList<String> thisTransform){
    ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
    float translateX = Float.parseFloat(thisTransform.get(0));
    float translateY = Float.parseFloat(thisTransform.get(1));
    float[][] translateMatrix = {{1, 0, translateX},{0, 1, translateY},{0, 0, 1}};
    matrixOperations.add(translateMatrix);
    return matrixOperations;
}

private static ArrayList<float[][]> scaleVertices(ArrayList<String> thisTransform){
    //SEE ROTATE COMMENTS
    //glTranslatef(Float.parseFloat(thisTransform.get(2)), Float.parseFloat(thisTransform.get(3)), 0f);
    //glScalef(Float.parseFloat(thisTransform.get(0)), Float.parseFloat(thisTransform.get(1)), 1f);
    //glTranslatef(-Float.parseFloat(thisTransform.get(2)), -Float.parseFloat(thisTransform.get(3)), 0f);   
    //
    //newVertices = multiply(translateInverse, thisVertice);
    //newVertices = multiply(scaleMatrix, newVertices);
    //newVertices = multiply(translateMatrix, newVertices);
    //return newVertices;
    
    ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
    float scaleX = Float.parseFloat(thisTransform.get(0));
    float scaleY = Float.parseFloat(thisTransform.get(1));
    float translateX = Float.parseFloat(thisTransform.get(2));
    float translateY= Float.parseFloat(thisTransform.get(3));
    float[][] translateMatrix = {{1, 0, translateX},{0, 1, translateY},{0, 0, 1}};
    float[][] translateInverse = {{1, 0, -translateX},{0, 1, -translateY},{0, 0, 1}};
    float[][] scaleMatrix = {{scaleX, 0, 0},{0, scaleY, 0},{0, 0, 1}};
    matrixOperations.add(translateInverse);
    matrixOperations.add(scaleMatrix);
    matrixOperations.add(translateMatrix);

    return matrixOperations;
}

private static ArrayList<float[][]> rotateVertices(ArrayList<String> thisTransform){
    //First Rendition: using gl transformation methods. Worked Successfully. Now to implement manual matrix trasnformations
    //glRotatef(Float.parseFloat(thisTransform.get(0)),Float.parseFloat(thisTransform.get(1)), Float.parseFloat(thisTransform.get(2)), 1f);
    //
    //
    //This was my second rendition: transform vertices one by one worked successfully. Now to make a transform composition matrix.
    //Returned the vertices as a 1d matrix
    //float[] newVertices = new float[3];
    //newVertices = multiply(translateInverse, thisVertice);
    //newVertices = multiply(rotateMatrix, newVertices);
    //newVertices = multiply(translateMatrix, newVertices);
    //
    //return newVertices
    ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
    float rotationAngleDegrees = Float.parseFloat(thisTransform.get(0));
    float rotationAngle = (float) Math.toRadians(rotationAngleDegrees);
    float pivPointX = Float.parseFloat(thisTransform.get(1));
    float pivPointY = Float.parseFloat(thisTransform.get(2));
    float[][] translateMatrix = {{1, 0, pivPointX},{0, 1, pivPointY},{0, 0, 1}};
    float[][] translateInverse = {{1, 0, -pivPointX},{0, 1, -pivPointY},{0, 0, 1}};
    float[][] rotateMatrix = {{(float)Math.cos(rotationAngle), -((float)Math.sin(rotationAngle)), 0},{(float) Math.sin(rotationAngle), (float)Math.cos(rotationAngle), 0},{0, 0, 1}};
    matrixOperations.add(translateInverse);
    matrixOperations.add(rotateMatrix);
    matrixOperations.add(translateMatrix);
    return matrixOperations;
}

private static float[][] makeCompositeMatrix(ArrayList<float[][]>matrixOperations){
    float[][] compositeMatrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    for (int i = 0; i < matrixOperations.size(); i++){
    compositeMatrix = multiply(matrixOperations.get(i), compositeMatrix);    
    }
    return compositeMatrix;

}
            
private static float[][] multiply(float[][] transformMatrixA, float[][] transformMatrixB){
    float[][] result = new float[3][3];
    
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            float sum = 0.0f;
            for (int k = 0; k < 3; k++) {
                sum += transformMatrixA[i][k] * transformMatrixB[k][j];
            }
            result[i][j] = sum;
        }
    }

    return result;
}

private static float[] multiply(float[][] transformMatrix, float[] verticesMatrix){
        float[] newVertices = new float[3];
        for (int i = 0; i < transformMatrix.length; i++){
        float sum = 0f;
        for (int j = 0; j < transformMatrix[i].length; j++){
            sum += transformMatrix[i][j] * verticesMatrix[j];
        }
        newVertices[i] = sum;
    }
  return newVertices;
}

private static void drawLines(ArrayList<Float> updatedPolygonVertices){
    ArrayList<ArrayList<Float>> allEdges = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<Float>> globalEdges = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<Float>> activeEdges = new ArrayList<ArrayList<Float>>();
    allEdges = makeAllEdges(updatedPolygonVertices);
    globalEdges = makeGlobalEdges(allEdges);
    System.out.print("Success");
}


private static ArrayList<ArrayList<Float>> makeAllEdges(ArrayList<Float> updatedPolygonVertices){
    ArrayList<ArrayList<Float>> allEdges = new ArrayList<ArrayList<Float>>();
    float xInit;
    float yInit;
    float xFinal;
    float yFinal;
    int rowEdgeTable = 0;
    allEdges.add(new ArrayList<Float>());

    
    for (int i = 0; i < updatedPolygonVertices.size(); i+=2){
        if (i == updatedPolygonVertices.size() - 2){
        xInit = updatedPolygonVertices.get(i);
        yInit = updatedPolygonVertices.get(i+1);
        xFinal = updatedPolygonVertices.get(0);
        yFinal = updatedPolygonVertices.get(1);
        }
        else{
        xInit = updatedPolygonVertices.get(i);
        yInit = updatedPolygonVertices.get(i+1);
        xFinal = updatedPolygonVertices.get(i+2);
        yFinal = updatedPolygonVertices.get(i+3);
        }
        if (yInit > yFinal){
            allEdges.get(rowEdgeTable).add(yFinal);
            allEdges.get(rowEdgeTable).add(yInit);
            allEdges.get(rowEdgeTable).add(xFinal);
            allEdges.get(rowEdgeTable).add((xInit-xFinal)/(yInit-yFinal));
        }
        else if (yFinal > yInit){
            allEdges.get(rowEdgeTable).add(yInit);
            allEdges.get(rowEdgeTable).add(yFinal);
            allEdges.get(rowEdgeTable).add(xInit);
            allEdges.get(rowEdgeTable).add((xInit-xFinal)/(yInit-yFinal));
        }
        rowEdgeTable++;
        allEdges.add(new ArrayList<Float>());
        }

    return allEdges;
}

private static ArrayList<ArrayList<Float>> makeGlobalEdges (ArrayList<ArrayList<Float>> allEdges){
    ArrayList<ArrayList<Float>> globalEdges = new ArrayList<ArrayList<Float>>();

    for (int i = 0; i < allEdges.size(); i++) {
        globalEdges.add(allEdges.get(i));
    }

    for (int i = 0; i < globalEdges.size() - 1; i++){
        for (int j = 0; j < globalEdges.size()-1-i; j++){
            ArrayList<Float> edge1 = globalEdges.get(j);
            ArrayList<Float> edge2 = globalEdges.get(j+1);
            boolean swap = false;

            if (edge1.get(0) > edge2.get(0)){
                swap = true;
            }
            else if (edge1.get(0).equals(edge2.get(0)) && edge1.get(2) > edge2.get(2)){
                swap = true;
            }
            else if (edge1.get(0).equals(edge2.get(0)) && edge1.get(2).equals(edge2.get(2)) && edge1.get(1) > edge2.get(1)){
                swap = true;
            }
            if (swap) {
                ArrayList<Float> temp = globalEdges.get(j);
                globalEdges.set(j, globalEdges.get(j+1));
                globalEdges.set(j+1, temp);
            }
        }
    }
    return globalEdges;
}


public static void main(String[] args) {
    start(args);
    }
}

