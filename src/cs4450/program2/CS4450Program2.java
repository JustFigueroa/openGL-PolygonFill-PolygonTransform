/***************************************************************
* file: CS4450Program2.java
* author: Justin Figueroa
* class: CS 4450 – Computer Graphics
*
* assignment: program 2
* date last modified: 09/29/2026
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
import java.nio.file.Path;
import java.nio.file.Paths;


public class CS4450Program2 {   
    
    
//method:getFile
//purpose: this method gets the file path from the data folder in src file of the program
//  return path to be parsed
public static String getFile() {
    Path path = Paths.get("src", "data", "coordinates.txt");
    return path.toString();
}

//method:parseCoordinateFile
//purpose: this method parses the passed file and returns "toDraw" a 2d dynamically allocated array of the passed
//  coordinates to be futher interpreted 
public static ArrayList<ArrayList<String>> parseCoordinateFile(String pathToFile){
    ArrayList<ArrayList<String>> toDraw = new ArrayList<ArrayList<String>>();
    toDraw.add(new ArrayList<String>());
    int rowArrayList = 0;
    try (Scanner fileScanner = new Scanner(new File(pathToFile))) {

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine().trim();
            if (line.isEmpty()){
                continue;
            }

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
        System.out.println ("Error Reading File, Please Ensure File is Formatted Correctly, and File is in Src/Data folder!");
    }
        return toDraw;        
}

//method:start
//purpose: this is the first method called to begin the process of drawing + filling polygons
//  from the coordinate text file
public static void start(){
    try{
        ArrayList<ArrayList<String>> toDraw;
        String path = getFile();        
        toDraw = parseCoordinateFile(path);
        createWindow();
        Keyboard.create();
        initGL();
        render(toDraw);
    }
    catch (Exception e){
        e.printStackTrace();
    }
}

//method: end
//purpose: closes the application when called
public static void end(){
    Display.destroy();
}

//method: createWindow
//purpose: createst the display window
 private static void createWindow() throws Exception{
    Display.setFullscreen(false);
    Display.setDisplayMode(new DisplayMode(640, 480));
    Display.setTitle("Program 2: Fill + Transform");
    Display.create();
}

 //method: initGL
 //purpose: initializes the window setting background color and initializes display orientation (coordinates)
private static void initGL(){
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(-320, 320, -240, 240, 1, -1);
    glMatrixMode(GL_MODELVIEW);
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
}    

//method: render
//purpose: render begins the process of drawing / filling polygons. 
//  begins the while loop that continuously draws until the window is closed
//  it accepts the parsed file from parseCoordinateFile method and passes it into the renderPolygon method
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

//method: renderpPolygon
//purpose: this function further parses the original passed data, accepting the 2d ArrayList and
//  seperating based on the indidcators P and T for the polygona color and vertices and transformations respectively
//  the parse seperates the data from P and T into ArrayLists polygonVertices and transformations to be interpreted further
//  by functions transformPolygonaVertices. Then the updatedPolygonaVertices and placed into a ArrayLis
//  to be drawn
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
            drawLines(updatedPolygonVertices);
            
        glEnd();
        glPopMatrix();
        
        }
    }
}

//method: transformPolygonVertices
//purpose: accepts the plygonVertices ArrayList and transformations ArrayList from renderPolygon method.
//  Then genetates transform matrices which are then proccesed into a compsite matrix.
//  The comosite matrix is used to perform the various transfromations on the provided polygonVertices 
//  and retrusn updated vertices as an ArrayList of floats
private static ArrayList<Float> transformPolygonVertices(ArrayList<String> polygonVertices, ArrayList<String> transformations){
        ArrayList<Float> updatedPolygonVertices = new ArrayList<Float>();
        ArrayList<String> thisTransform = new ArrayList<String>();
        ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
        ArrayList<float[][]> thisOperations = new ArrayList<float[][]>();
        float[][] compositeMatrix = new float[3][3];
        float[] newVertices = new float[3];
        float[] thisVertices = new float[3];

try{
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
catch (Exception e){
    System.out.println ("Error Reading File, Please Ensure File is Formatted Correctly, and Path to File is Accurate!");
}
return updatedPolygonVertices;
}

//method: trasnlatesVertices
//purpose: accepts a specific transformation from the transformPolygonVertices method
//  parses transformation specifics and generates and returns a 2d array trasnformation (trasnlation) matrix
//  returned as an ArrayList of trasnformations because scale and rotate require tranlsation to pivot points
 private static ArrayList<float[][]> translateVertices(ArrayList<String> thisTransform){
    ArrayList<float[][]> matrixOperations = new ArrayList<float[][]>();
    float translateX = Float.parseFloat(thisTransform.get(0));
    float translateY = Float.parseFloat(thisTransform.get(1));
    float[][] translateMatrix = {{1, 0, translateX},{0, 1, translateY},{0, 0, 1}};
    matrixOperations.add(translateMatrix);
    return matrixOperations;
}

//method: scaleVertices
//purpose: accepts a specific transformation from the transformPolygonVertices method
//  parses transformation specifics and generates and returns a 2d array trasnformation (scale) matrix 
//  returned as an ArrayList of trasnformations because scale and rotate require tranlsation to pivot points
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

//method: rotateVertices
//purpose: accepts a specific transformation from the transformPolygonVertices method
//  parses transformation specifics and generates and returns a 2d array trasnformation (rotation) matrix 
//  returned as an ArrayList of trasnformations because scale and rotate require tranlsation to pivot points
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

//method: makeCompositeMatrix
//purpose: this matrix accepts the ArrayList of 2d matrices, the various transform matrices created in the previous methods
//  called from transformPolygonVertices method. Creates an identity matrix that is then passed with the various transformations into 
//  a matrix multiplication algortihm method
private static float[][] makeCompositeMatrix(ArrayList<float[][]>matrixOperations){
    float[][] compositeMatrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    for (int i = 0; i < matrixOperations.size(); i++){
    compositeMatrix = multiply(matrixOperations.get(i), compositeMatrix);    
    }
    return compositeMatrix;

}

//method: multiply (2d * 2d overload method)
//purpose: multiplies 2d matrices returns a 2d matrix. Used to create composite matrix.
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

//method: multiply (1d * 2d method)
//purpose: multiplies 1d matrix containing a specific pair of vertices by the composite matrix created by makeCompositeMatrix method
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

//method: drawLines
//purpose: this method is doing the drawing and filling of the polygons. It uses a bubble sort algorithm to sort activeEdges and the 
//algorithm discussed in class to plot the updated vertices (passed as ArrayList of floats created in the trasformPolygonaVertices method.
//Used Edge tables and scan line.
private static void drawLines(ArrayList<Float> updatedPolygonVertices){
    ArrayList<ArrayList<Float>> allEdges = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<Float>> globalEdges = new ArrayList<ArrayList<Float>>();
    ArrayList<ArrayList<Float>> activeEdges = new ArrayList<ArrayList<Float>>();
    allEdges = makeAllEdges(updatedPolygonVertices);
    globalEdges = makeGlobalEdges(allEdges);
    try{
    int scanLine = (int)Math.ceil(globalEdges.get(0).get(0));

    glBegin(GL_POINTS);
    
    while (globalEdges.size() > 0 || activeEdges.size() > 0){
        
        while (globalEdges.size() > 0 && (int)Math.ceil(globalEdges.get(0).get(0)) == scanLine){

            ArrayList<Float> edge = new ArrayList<Float>(globalEdges.get(0));

            float minY = edge.get(0);
            float xMin = edge.get(2);
            float inverseSlope = edge.get(3);
            //this is slightly modified due to ciel conversion because of floating point inaccuracy
            float currentX = xMin + (scanLine - minY) * inverseSlope;

            edge.set(2, currentX);

            activeEdges.add(edge);

            globalEdges.remove(0);
        }
        //remove from active edge if scanLine > maxy
        for (int i = activeEdges.size() - 1; i >= 0; i--){
            if (scanLine >= activeEdges.get(i).get(1)){
                activeEdges.remove(i);
            }

        }
        //same algo used to sort global edges
        for (int i = 0; i < activeEdges.size()-1; i++){
            for (int j = 0; j < activeEdges.size()-1-i; j++){
                if (activeEdges.get(j).get(2) > activeEdges.get(j + 1).get(2)){
                   
                    ArrayList<Float> temp = activeEdges.get(j);
                    activeEdges.set(j, activeEdges.get(j+1));
                    activeEdges.set(j+1, temp);

                }
            }
        }
        
        //fill
        int parity = 0;

        for (int i = 0; i < activeEdges.size(); i++){
            if (parity == 0){
                
                float xStart = activeEdges.get(i).get(2);
                if (i+1 < activeEdges.size()){

                    float xEnd = activeEdges.get(i+1).get(2);
                    int startPixel = (int)Math.ceil(xStart);

                    int endPixel = (int)Math.floor(xEnd);

                    for (int x = startPixel; x <= endPixel; x++){

                        glVertex2f((float)x, (float)scanLine);
                    }
                }
                parity = 1;
            }
            else{
                parity = 0;
            }

        }

        //update x1 = x0 + 1/m
        for (int i = 0; i < activeEdges.size(); i++){
            float currentX = activeEdges.get(i).get(2);
            float inverseSlope = activeEdges.get(i).get(3);
            activeEdges.get(i).set(2,currentX + inverseSlope);
        }
        scanLine++;
    }
    glEnd();
    }
    catch (Exception e){
        System.out.println ("Error Reading File, Please Ensure File is Formatted Correctly, and Path to File is Accurate!");
    }
}

//method: makeAllEdges
//purpose: this method creates the allEdges table. Uses algorithm discussed in class to  parse and sort the 
//  edges. return the allEdge table in the form of a 2d ArrayList of type float
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
        if (i < updatedPolygonVertices.size() - 2){
        allEdges.add(new ArrayList<Float>());    
        }
        }

    return allEdges;
}

//method: makeGlobalEdges
//purpose: accepts the allEdges table and sorts its based in class algorithm using a bubble sort algorithm. 
//  returns a 2d ArrayList of type float as the globalEdge table
private static ArrayList<ArrayList<Float>> makeGlobalEdges (ArrayList<ArrayList<Float>> allEdges){
    ArrayList<ArrayList<Float>> globalEdges = new ArrayList<ArrayList<Float>>();

    for (int i = 0; i < allEdges.size(); i++) {
        if (!allEdges.get(i).isEmpty())
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

//method: main
//purpose: calls the start method which begin the entire program
public static void main(String[] args) {
    start();
    }
}

