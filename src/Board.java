import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;

public class Board extends JPanel implements MouseListener{
    private int sideLength; // side length of Board by number of squares (standard: 20)
    private Tile[][] Board;
    private Tile t; // stores a tile for third party usage
    private int screenW, screenH;
    private Image img;
    private int squareSize;
    private int xRelative, yRelative; //position of the black background
    /** I WOULD LIKE TO TURN THIS INTO A LIST RATHER THAN ARRAYLIST **/
    private ArrayList<Destinations> listOfGoals; //index 0 is the start Dest. and index 1 is the end index
    private int backTrackX, backTrackY; //used for reverse tracing the path for the best path (the last square before reaching the destination)
    private int xMouse, yMouse;
    private String optionType = "";
    private Image background;
    private int buttonSelect;

    //sound clip variables
    private Clip buttonClickedSound;
    private Clip squarePlacedSound;

    //these variables are used for picking the best path
    private boolean retracePath;
    private int holdXBT, holdYBT;


    //variables based on the buttons
    private boolean runPath;

    // initialize all variables above
    public Board(int sLength) {
        //enables the mouse
        this.addMouseListener(this);
        setFocusable(true);

        buttonSelect = 0; //0 = nothing selected, 1 = "run path", 2 = "Walls", 3 = "Empty", 4 = "Goals"
        retracePath = false;
        holdXBT = -1;
        holdYBT = -1;
        runPath = false;
        backTrackX = -1;
        backTrackY = -1;
        xMouse = -1;
        yMouse = -1;

        // implements the images and sounds
        try {
            background = ImageIO.read(new File("src/MazeBackground.jpg"));

            buttonClickedSound = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("buttonClicked.wav"));
            buttonClickedSound.open(inputStream);
            buttonClickedSound.setFramePosition(0);

            squarePlacedSound = AudioSystem.getClip();
            AudioInputStream inputStream2 = AudioSystem.getAudioInputStream(new File("placingSquares.wav"));
            squarePlacedSound.open(inputStream2);
            squarePlacedSound.setFramePosition(0);

        } catch (Exception e) {}

        listOfGoals = new ArrayList<>();
        //the following two sizes are arbitrary numbers solely based for centering on GCC 2021 laptop
        xRelative = 340 + 1; //adds one to make sure there is a black divider between squares
        yRelative = 30 + 1;

        /** IMPLEMENTS THE MAP **/
        if (sLength < 600 && ((600.0 / sLength) % 1 == 0) && (600 / sLength) - 2 > 0)
            sideLength = sLength;
        else
            sideLength = 20;
        squareSize = (600 / sideLength) - 2; //subtracts 2 to make sure there is space inbetween squares
        Board = new Tile[sideLength][sideLength];

        for (int x = 0; x < sideLength; x++) {
            for (int y = 0; y < sideLength; y++) {
                if (x == 0 || x == sideLength - 1 || y == 0 || y == sideLength - 1)
                    Board[x][y] = new borderWall(x, y, squareSize);
                else
                    Board[x][y] = new Empty(x, y, squareSize);

            }
        }

    }


    // for GUI
    @Override
    public void paintComponent(Graphics g) {
        //initiates the size of the window based off the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenW = (int) (screenSize.getWidth()); // 1280 --> 1280 - 600 = 680 / 2 = 340
        screenH = (int) (screenSize.getHeight()); // 720 --> 600 / 20 = 30 -- 28 x 28 blocks

        img = createImage(screenW, screenH);

        //img can be edited using "gr"
        Graphics gr = img.getGraphics();

        //updates the board
        drawBoard(gr);

        //finds the shortest path between the first & second element in the arrayList
        if (listOfGoals.size() >= 2 && runPath == true) {
            if (!retracePath) {
                pathFinder();
            }

            // redraws the best path for the user
            if (listOfGoals.size() > 1 && (backTrackX != listOfGoals.get(0).getX() || backTrackY != listOfGoals.get(0).getY())){
                holdXBT = backTrackX;
                holdYBT = backTrackY;
                backTrackX = Board[holdXBT][holdYBT].getX_origin();
                backTrackY = Board[holdXBT][holdYBT].getY_origin();
                Board[holdXBT][holdYBT].setColor(Color.green);

                //
                Board[holdXBT][holdYBT].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                //
                wait(50);
                repaint();
            }else {
                retracePath = false;
                buttonSelect = 0;
            }

            if (listOfGoals.size() > 0 && retracePath == false) {
                listOfGoals.remove(0);
                runPath = false;
            }
        }
        //actually draws the image through the method argument "g"
        g.drawImage(img, 0, 0, null);

        repaint();
    }

    // USES THE 1ST & 2ND INDEX IN THE ARRAY LIST OF listOfGoals to find the shortest path
    private void pathFinder(){
        // X & Y VALUES ARE SET TO STARTING POSITION
        int x_current = listOfGoals.get(0).getX();
        int y_current = listOfGoals.get(0).getY();
        HashSet<Tile> listOfPossiblePaths = new HashSet<>();

        // While the square (x_current, y_current) is not a destination
        while (x_current != listOfGoals.get(1).getX() || y_current != listOfGoals.get(1).getY()) {
            /** CHECKS RIGHT **/
             if (Board[x_current + 1][y_current] instanceof Empty ||
                     Board[x_current + 1][y_current] instanceof Destinations){
                 if(Board[x_current + 1][y_current] instanceof Empty){
                     Board[x_current + 1][y_current] =
                             new PossiblePath(x_current + 1, y_current, x_current, y_current, squareSize);
                     Board[x_current + 1][y_current].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                     /** position [x+1][y] is set to the value of (what [x+1][y] should be given [x][y]) **/
                     Board[x_current + 1][y_current].setSCost(Board[x_current + 1][y_current].determineSCost(Board[x_current][y_current]));
                     Board[x_current + 1][y_current].setECost();
                 }
                 listOfPossiblePaths.add(Board[x_current + 1][y_current]);
             } else if (Board[x_current + 1][y_current] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current + 1][y_current]).getSCost() >
                            Board[x_current + 1][y_current].determineSCost(Board[x_current][y_current])) {
                 /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                 Board[x_current + 1][y_current].setSCost(Board[x_current + 1][y_current].determineSCost(Board[x_current][y_current]));
                 Board[x_current + 1][y_current].changeXYOrigin(x_current, y_current);
             }
            /** CHECKS LEFT **/
            if (Board[x_current - 1][y_current] instanceof Empty ||
                    Board[x_current - 1][y_current] instanceof Destinations){
                if(Board[x_current - 1][y_current] instanceof Empty){
                    Board[x_current - 1][y_current] =
                            new PossiblePath(x_current - 1, y_current, x_current, y_current, squareSize);
                    Board[x_current - 1][y_current].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current - 1][y_current].setSCost(Board[x_current - 1][y_current].determineSCost(Board[x_current][y_current]));
                    Board[x_current - 1][y_current].setECost();
                }
                listOfPossiblePaths.add(Board[x_current - 1][y_current]);
            } else if (Board[x_current - 1][y_current] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current - 1][y_current]).getSCost() >
                            Board[x_current - 1][y_current].determineSCost(Board[x_current][y_current])) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current - 1][y_current].setSCost(Board[x_current - 1][y_current].determineSCost(Board[x_current][y_current]));
                Board[x_current - 1][y_current].changeXYOrigin(x_current, y_current);
            }
            /** CHECKS DOWN **/
            if (Board[x_current][y_current + 1] instanceof Empty ||
                    Board[x_current][y_current + 1] instanceof Destinations){
                if(Board[x_current][y_current + 1] instanceof Empty){
                    Board[x_current][y_current + 1] =
                            new PossiblePath(x_current, y_current + 1, x_current, y_current, squareSize);
                    Board[x_current][y_current + 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current][y_current + 1].setSCost(Board[x_current][y_current + 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current][y_current + 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current][y_current + 1]);
            } else if (Board[x_current][y_current + 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current][y_current + 1]).getSCost() >
                            Board[x_current][y_current + 1].determineSCost(Board[x_current][y_current])) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current][y_current + 1].setSCost(Board[x_current][y_current + 1].determineSCost(Board[x_current][y_current]));
                Board[x_current][y_current + 1].changeXYOrigin(x_current, y_current);
            }
            /** CHECKS UP **/
            if (Board[x_current][y_current - 1] instanceof Empty ||
                    Board[x_current][y_current - 1] instanceof Destinations){
                if(Board[x_current][y_current - 1] instanceof Empty){
                    Board[x_current][y_current - 1] =
                            new PossiblePath(x_current, y_current - 1, x_current, y_current, squareSize);
                    Board[x_current][y_current - 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current][y_current - 1].setSCost(Board[x_current][y_current - 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current][y_current - 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current][y_current - 1]);
            } else if (Board[x_current][y_current - 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current][y_current - 1]).getSCost() >
                            Board[x_current][y_current - 1].determineSCost(Board[x_current][y_current])) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current][y_current - 1].setSCost(Board[x_current][y_current - 1].determineSCost(Board[x_current][y_current]));
                Board[x_current][y_current - 1].changeXYOrigin(x_current, y_current);
            }

            /** ---------------------------------------DIAGONALS--------------------------------------- **/
            /** CHECKS UP & LEFT **/
            if ((Board[x_current - 1][y_current - 1] instanceof Empty ||
                    Board[x_current - 1][y_current - 1] instanceof Destinations) &&
                    (!(Board[x_current - 1][y_current] instanceof  Wall) || !(Board[x_current][y_current - 1] instanceof Wall))){
                if(Board[x_current - 1][y_current - 1] instanceof Empty){
                    Board[x_current - 1][y_current - 1] =
                            new PossiblePath(x_current - 1, y_current - 1, x_current, y_current, squareSize);
                    Board[x_current - 1][y_current - 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current - 1][y_current - 1].setSCost(Board[x_current - 1][y_current - 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current - 1][y_current - 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current - 1][y_current - 1]);
            } else if (Board[x_current - 1][y_current - 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current - 1][y_current - 1]).getSCost() >
                            Board[x_current - 1][y_current - 1].determineSCost(Board[x_current][y_current]) &&
                        (!(Board[x_current - 1][y_current] instanceof  Wall) || !(Board[x_current][y_current - 1] instanceof Wall))) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current - 1][y_current - 1].setSCost(Board[x_current - 1][y_current - 1].determineSCost(Board[x_current][y_current]));
                Board[x_current - 1][y_current - 1].changeXYOrigin(x_current, y_current);
            }
            /** CHECKS UP & RIGHT **/
            if ((Board[x_current + 1][y_current - 1] instanceof Empty ||
                    Board[x_current + 1][y_current - 1] instanceof Destinations) &&
                    (!(Board[x_current + 1][y_current] instanceof Wall) || !(Board[x_current][y_current - 1] instanceof Wall))){
                if(Board[x_current + 1][y_current - 1] instanceof Empty){
                    Board[x_current + 1][y_current - 1] =
                            new PossiblePath(x_current + 1, y_current - 1, x_current, y_current, squareSize);
                    Board[x_current + 1][y_current - 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current + 1][y_current - 1].setSCost(Board[x_current + 1][y_current - 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current + 1][y_current - 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current + 1][y_current - 1]);
            } else if (Board[x_current + 1][y_current - 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current + 1][y_current - 1]).getSCost() >
                            Board[x_current + 1][y_current - 1].determineSCost(Board[x_current][y_current]) &&
                        (!(Board[x_current + 1][y_current] instanceof Wall) || !(Board[x_current][y_current - 1] instanceof Wall))) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current + 1][y_current - 1].setSCost(Board[x_current + 1][y_current - 1].determineSCost(Board[x_current][y_current]));
                Board[x_current + 1][y_current - 1].changeXYOrigin(x_current, y_current);
            }
            /** CHECKS DOWN & LEFT **/
            if ((Board[x_current - 1][y_current + 1] instanceof Empty ||
                    Board[x_current - 1][y_current + 1] instanceof Destinations) &&
                        (!(Board[x_current - 1][y_current] instanceof Wall) || !(Board[x_current][y_current + 1] instanceof Wall))){
                if(Board[x_current - 1][y_current + 1] instanceof Empty){
                    Board[x_current - 1][y_current + 1] =
                            new PossiblePath(x_current - 1, y_current + 1, x_current, y_current, squareSize);
                    Board[x_current - 1][y_current + 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current - 1][y_current + 1].setSCost(Board[x_current - 1][y_current + 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current - 1][y_current + 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current - 1][y_current + 1]);
            } else if (Board[x_current - 1][y_current + 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current - 1][y_current + 1]).getSCost() >
                            Board[x_current - 1][y_current + 1].determineSCost(Board[x_current][y_current]) &&
                        (!(Board[x_current - 1][y_current] instanceof Wall) || !(Board[x_current][y_current + 1] instanceof Wall))) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current - 1][y_current + 1].setSCost(Board[x_current - 1][y_current + 1].determineSCost(Board[x_current][y_current]));
                Board[x_current - 1][y_current + 1].changeXYOrigin(x_current, y_current);
            }
            /** CHECKS DOWN & RIGHT **/
            if ((Board[x_current + 1][y_current + 1] instanceof Empty ||
                    Board[x_current + 1][y_current + 1] instanceof Destinations) &&
                        (!(Board[x_current + 1][y_current] instanceof Wall) || !(Board[x_current][y_current + 1] instanceof Wall))){
                if(Board[x_current + 1][y_current + 1] instanceof Empty){
                    Board[x_current + 1][y_current + 1] =
                            new PossiblePath(x_current + 1, y_current + 1, x_current, y_current, squareSize);
                    Board[x_current + 1][y_current + 1].setSF(listOfGoals.get(0).getX(), listOfGoals.get(0).getY(), listOfGoals.get(1).getX(), listOfGoals.get(1).getY());
                    /** position [x-1][y] is set to the value of (what [x-1][y] should be given [x][y]) **/
                    Board[x_current + 1][y_current + 1].setSCost(Board[x_current + 1][y_current + 1].determineSCost(Board[x_current][y_current]));
                    Board[x_current + 1][y_current + 1].setECost();
                }
                listOfPossiblePaths.add(Board[x_current + 1][y_current + 1]);
            } else if (Board[x_current + 1][y_current + 1] instanceof PossiblePath &&
                    ((PossiblePath) Board[x_current + 1][y_current + 1]).getSCost() >
                            Board[x_current + 1][y_current + 1].determineSCost(Board[x_current][y_current]) &&
                        (!(Board[x_current + 1][y_current] instanceof Wall) || !(Board[x_current][y_current + 1] instanceof Wall))) {
                /** IF THE CURRENT POSSIBLE PATH HAS A LARGER SCOST, THEN REPLACE IT**/
                Board[x_current + 1][y_current + 1].setSCost(Board[x_current + 1][y_current + 1].determineSCost(Board[x_current][y_current]));
                Board[x_current + 1][y_current + 1].changeXYOrigin(x_current, y_current);
            }

             /** DETERMINES THE NEXT SQUARE TO MOVE ONTO **/
            //if the destination is trapped
            if (listOfPossiblePaths.isEmpty()) {
                clearBoardOrReset(false);
                break;
            } else {
                //Takes best position and "moves" there
                Tile t = smallestTvalue(listOfPossiblePaths); //the object on the board with the smallest TValue
                listOfPossiblePaths.remove(t); // makes it so this square on the path can't be moved back to
                int x = t.getX(), y = t.getY();
                if (Board[x][y] instanceof Destinations) {
                    //these two variables is the position of the square right before the destination
                    backTrackX = x_current;
                    backTrackY = y_current;

                }
                x_current = x;
                y_current = y;
            }
            retracePath = true;
        }

    }

    //Draws the board
    private void drawBoard(Graphics g) {
        //sets the background for the map
        g.drawImage(background, 0, 0, null);

        int spacer = 20;
        int buttonH = 40;
        int newXRel = 600 + xRelative + 50; //size of the board + the xRelative + some extra space
        int newYRel = 160;
        //Find Path Button
        if (buttonSelect == 1)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (0), 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Find Path", newXRel + 17, newYRel + 27);

        //Wall Button
        if (buttonSelect == 2)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (spacer + buttonH), 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Wall", newXRel + 40, newYRel + (spacer + buttonH) + 27);

        //Empty Button
        if (buttonSelect == 3)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (spacer + buttonH) * 2, 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Erase", newXRel + 35, newYRel + (spacer + buttonH) * 2 + 27);

        //Destination Button
        if (buttonSelect == 4)
            g.setColor(Color.YELLOW);
        else
            g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (spacer + buttonH) * 3, 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Destination", newXRel + 7, newYRel + (spacer + buttonH) * 3 + 27);

        //Clear all button
        g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (spacer + buttonH) * 4, 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Reset", newXRel + 35, newYRel + (spacer + buttonH) * 4 + 27);

        //Clear all button
        g.setColor(Color.WHITE);
        g.fillRect(newXRel, newYRel + (spacer + buttonH) * 5, 120, buttonH);
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Clear All", newXRel + 20, newYRel + (spacer + buttonH) * 5 + 27);

        g.setColor(Color.black);
        g.fillRect(340, 30, 600, 600);

        //draws the grid of the board
        for (int x = 0; x < sideLength; x++) {
            for (int y = 0; y < sideLength; y++) {
                Board[x][y].draw(squareSize, g);
            }
        }
    }







    /**
     * finds the lowest TValue on the board
     *
     * @return tile with the lowest TValue
     */
    public Tile smallestTvalue(HashSet<Tile> possiblePaths) {
        Tile moveTo = null;
        double min = sideLength * sideLength + 1;
        for (Tile tp : possiblePaths) {
            if(moveTo == null || (tp.getTValue() != -1 && tp.getTValue() < min)){
                moveTo = tp;
                min = tp.getTValue();
            }
        }
        return moveTo;
    }

    /**
     * Clears all possible paths from the board
     */
    private void clearPossiblePaths() {
        for (int x = 0; x < sideLength - 1; x++) {
            for (int y = 0; y < sideLength - 1; y++) {
//                if (Board[x][y] instanceof PossiblePath)
//                    Board[x][y] = new Empty(x, y, squareSize);
            }
        }
    }

    //Allows pauses in the code
    private void wait(int time) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < time) {
        }
    }

    //either clears the baord and resets it for a new map OR resets everything but the walls
    private void clearBoardOrReset(boolean clearBoardEntirely) {
        for (int x = 0; x < sideLength; x++) {
            for (int y = 0; y < sideLength; y++) {
                if (x == 0 || x == sideLength - 1 || y == 0 || y == sideLength - 1)
                    Board[x][y] = new borderWall(x, y, squareSize);
                else if (!clearBoardEntirely && !(Board[x][y] instanceof Wall))
                    Board[x][y] = new Empty(x, y, squareSize);
                else if (clearBoardEntirely)
                    Board[x][y] = new Empty(x, y, squareSize);
            }
        }
        listOfGoals.clear();
        runPath = false;
        retracePath = false;
        holdXBT = -1;
        holdYBT = -1;
        backTrackX = -1;
        backTrackY = -1;
        buttonSelect = 0;
    }

    /** CLICKING CLICKING CLICKING CLICKING CLICKING **/

    //plays a sound
    private void buttonClicked(Clip sound) {
        Thread t = new Thread() {
            public void run() {
                sound.start();
            }
        };
        t.start();
        sound.setFramePosition(0);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        xMouse = e.getX();
        yMouse = e.getY();

        if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > 160 && yMouse < 160 + 40) {
            buttonClicked(buttonClickedSound);
            runPath = true;
            buttonSelect = 1;
        } else if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > (160 + 60) && yMouse < (160 + 60) + 40) {
            buttonClicked(buttonClickedSound);
            optionType = "Wall";
            buttonSelect = 2;
        } else if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > (160 + 60 * 2) && yMouse < (160 + 60 * 2) + 40) {
            buttonClicked(buttonClickedSound);
            optionType = "Empty";
            buttonSelect = 3;
        } else if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > (160 + 60 * 3) && yMouse < (160 + 60 * 3) + 40) {
            buttonClicked(buttonClickedSound);
            optionType = "Destination";
            buttonSelect = 4;
        } else if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > (160 + 60 * 4) && yMouse < (160 + 60 * 4) + 40) {
            buttonClicked(buttonClickedSound);
            clearBoardOrReset(false);
        } else if (xMouse > 650 + xRelative && xMouse < 650 + xRelative + 120 && yMouse > (160 + 60 * 5) && yMouse < (160 + 60 * 5) + 40) {
            buttonClicked(buttonClickedSound);
            clearBoardOrReset(true);
        }

        // ensures user is clicking within the grid first
        if (xMouse > xRelative && xMouse < 600 + xRelative && yMouse > yRelative && yMouse < yRelative + 600) {
            int xSquareClicked = (xMouse - xRelative) / (squareSize + 2);
            int ySquareClicked = (yMouse - yRelative) / (squareSize + 2);
            if (Board[xSquareClicked][ySquareClicked].isErasable()) {
                switch (optionType) {
                    case "":
                        break;
                    case "Wall":
                        buttonClicked(squarePlacedSound);
                        if (Board[xSquareClicked][ySquareClicked] instanceof Destinations) {
                            listOfGoals.remove(Board[xSquareClicked][ySquareClicked]);
                        }
                        Board[xSquareClicked][ySquareClicked] = new Wall(xSquareClicked, ySquareClicked, squareSize);
                        break;
                    case "Empty":
                        buttonClicked(squarePlacedSound);
                        if (Board[xSquareClicked][ySquareClicked] instanceof Destinations) {
                            listOfGoals.remove(Board[xSquareClicked][ySquareClicked]);
                        }
                        Board[xSquareClicked][ySquareClicked] = new Empty(xSquareClicked, ySquareClicked, squareSize);
                        break;
                    case "Destination":
                        buttonClicked(squarePlacedSound);
                        if (listOfGoals.size() < 2 && !(Board[xSquareClicked][ySquareClicked] instanceof Destinations)) {
                            Board[xSquareClicked][ySquareClicked] = new Destinations(xSquareClicked, ySquareClicked, squareSize);
                            listOfGoals.add((Destinations) (Board[xSquareClicked][ySquareClicked]));
                        }
                        break;
                    default:
                }
            }
            repaint();
        }
        xMouse = -1;
        yMouse = -1;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
