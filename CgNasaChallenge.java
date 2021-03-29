import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CgNasaChallenge {
   // Make coordinate X,Y pair into an inner class
    private class Coordinate {
        int x;
        int y;
        //Getters and setters
        public void setXY(int x, int y){
            this.x = x;
            this.y = y;
        }
        public int getX(){
            return this.x;
        }
        public int getY(){
            return this.y;
        }
    }

    // Make initial coordinate, direction, and move instruction into an inner class
    private class InitialPosition {
        Coordinate initialCoordinate = new Coordinate();
        String initialDirection = new String();
        String moveInstruction = new String();
        //Getters and Setters
        public void setInitialCoordinate(Coordinate initialCoordinate){
            this.initialCoordinate = initialCoordinate;
        }
        public Coordinate getInitialCoordinate(){
            return this.initialCoordinate;
        }
        public void setInitialDirection(String initialDirection){
            this.initialDirection = initialDirection;
        }
        public String getInitialDirection(){
            return this.initialDirection;
        }
        public void setMoveInstruction(String moveInstruction){
            this.moveInstruction = moveInstruction;
        }
        public String getMoveInstruction(){
            return this.moveInstruction;
        }
    }
    private Coordinate maxSurface = new Coordinate();
    private List<InitialPosition> initialPositions = new ArrayList<InitialPosition>();
    private Map<String, Coordinate> oneMovePerDirection = new HashMap<String, Coordinate>();
    private Map<String, String> oneRotation = new HashMap<String, String>();

    //Constructor
    public CgNasaChallenge(){}

    public void readParamFile() throws IOException {
        // Reads test.txt file line by line, expecting all input values
        // have no leading space, and first and second lines areseparated by a space 
        // and the third line is one string without any spaces such as:
        // first line :5 5
        // second line:1 2 N
        // third line :MLLRMR
        String fileName = "test.txt";
        BufferedReader br = new BufferedReader(new FileReader(fileName));
         
        try {
            String oneLine = br.readLine();
            if (oneLine != null) {
                int x = Integer.parseInt(oneLine.substring(0,1));
                int y = Integer.parseInt(oneLine.substring(2,3));
                maxSurface.setXY(x,y);

                // Read next line and iterate only when first line has coordinate info
                String nextLine = br.readLine();
                while(nextLine != null){
                    InitialPosition initialPosition = new InitialPosition();
                    Coordinate coordinate = new Coordinate();
                    x = Integer.parseInt(nextLine.substring(0,1));
                    y = Integer.parseInt(nextLine.substring(2,3));
                    coordinate.setXY(x,y);
                    initialPosition.setInitialCoordinate(coordinate);
                    initialPosition.setInitialDirection(nextLine.substring(4,5));

                    String moveInstruction = br.readLine(); //read move instruction line
                    initialPosition.setMoveInstruction(moveInstruction);
                    initialPositions.add(initialPosition);
                    nextLine = br.readLine();
                }
            }
        } finally {
            br.close();
        }    
    }

    public void setDirectionMap(){
        // Store one move per direction in a map
        Coordinate coordinateN = new Coordinate();
        coordinateN.setXY(0,1);
        oneMovePerDirection.put("N", coordinateN);
        Coordinate coordinateE = new Coordinate();
        coordinateE.setXY(1,0);
        oneMovePerDirection.put("E", coordinateE);
        Coordinate coordinateW = new Coordinate();
        coordinateW.setXY(-1,0);
        oneMovePerDirection.put("W", coordinateW);
        Coordinate coordinateS = new Coordinate();
        coordinateS.setXY(0,-1);
        oneMovePerDirection.put("S", coordinateS);

        // Store direction where one rotation points
        oneRotation.put("NR", "E"); //one rotation from North to the Right points East
        oneRotation.put("NL", "W"); //one rotation from North to the Left points West
        oneRotation.put("ER", "S"); //one rotation from East to the Right points South
        oneRotation.put("EL", "N"); //one rotation from East to the Left points North
        oneRotation.put("SR", "W"); //one rotation from South to the Right points West
        oneRotation.put("SL", "E"); //one rotation from South to the Left points East
        oneRotation.put("WR", "N"); //one rotation from West to the Right points North
        oneRotation.put("WL", "S"); //one rotation from West to the Left points South
    }

    public void setCoordinates(){
        // Repeat as many as there are rovers
        int currentRover = -1;
        for (InitialPosition initialPosition: initialPositions){
            currentRover++;
            //InitialPosition initialPosition = initialPositions.get(i);
            String directionChangedTo = initialPosition.getInitialDirection();
            int xChangedTo = initialPosition.getInitialCoordinate().getX();
            int yChangedTo = initialPosition.getInitialCoordinate().getY();
            //Iterate through the letters in move instruction
            for (int j=0; j<initialPosition.getMoveInstruction().length(); j++){
                if ("R".equals(initialPosition.getMoveInstruction().substring(j, j+1))){
                   directionChangedTo = oneRotation.get(directionChangedTo + "R");
                } else if ("L".equals(initialPosition.getMoveInstruction().substring(j, j+1))) {
                    directionChangedTo = oneRotation.get(directionChangedTo + "L");           
                } else if ("M".equals(initialPosition.getMoveInstruction().substring(j, j+1))) {
                    xChangedTo = xChangedTo + oneMovePerDirection.get(directionChangedTo).getX();
                    yChangedTo = yChangedTo + oneMovePerDirection.get(directionChangedTo).getY();
                    // Check to see if the rover is outside of the coordinates
                    if (xChangedTo < 0 || xChangedTo > maxSurface.getX() || 
                        yChangedTo < 0 || yChangedTo > maxSurface.getY()) {
                        System.out.println("Rover #" + currentRover + " has gone outside of the surface. Exiting.");                        
                        return;
                    }
                    if (checkCollisions(xChangedTo, yChangedTo, currentRover, initialPositions)){
                        System.out.println("Rover #" + currentRover + " has collied with other rover. Exiting."); 
                        return;
                    }
                }
            }
            // Display rover's last position
            System.out.println(String.valueOf(xChangedTo) + " " + 
                               String.valueOf(yChangedTo) + " " +
                               directionChangedTo);

            //Update the last position of the current rover (so that we can check for the collision)
            Coordinate finalCoordinate = new Coordinate();
            finalCoordinate.setXY(xChangedTo, yChangedTo);
            InitialPosition finalPosition = new InitialPosition();
            finalPosition.setInitialCoordinate(finalCoordinate);
            finalPosition.setInitialDirection(directionChangedTo);
            finalPosition.setMoveInstruction("");
            initialPositions.set(currentRover, finalPosition);           
        }
    }

    public boolean checkCollisions (int x, int y, int currentRover, List<InitialPosition> initialPositions){
        boolean collided = false;
        int lineNumber = -1;
        for (InitialPosition initialPosition: initialPositions){
            lineNumber++;
            if (lineNumber == currentRover){
                continue;
            }
            //InitialPosition initialPosition = initialPositions.get(i);
            Coordinate coordinate = initialPosition.getInitialCoordinate();
            if(x == coordinate.getX() && y == coordinate.getY()){
               collided = true;
            }
        }
        return collided;
    }

    public static void main(String[] args){
        try {
            CgNasaChallenge cgNasaChallenge = new CgNasaChallenge();
            cgNasaChallenge.readParamFile();
            cgNasaChallenge.setDirectionMap();
            cgNasaChallenge.setCoordinates();
        } catch(IOException ioe){
            System.out.println("test.txt file does not exist");
        }
    }
}