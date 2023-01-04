import java.awt.*;

abstract class Tile {
    final private boolean erasable; // whether the tile is a border, in which case it cannot be changed by the user
    private double Tvalue; // combined value of the tile's value from the distance and the start
    public int x, y; // the tile's x and y coordinates, respectively
    protected Color c; // the tile's color (for the GUI)
    protected int xRelative, yRelative, squareSize; // used for drawing the shapes
    protected int x_origin, y_origin; // these variables are used for "PossiblePath" to help backtrack once final destination is found

    // SCost is distance from starting node (following the path of prior nodes)
    // ECost is the distance to the end node
    protected double SCost = -1;
    protected double ECost = -1;

    public Tile(boolean erasable, double Tvalue, int x, int y, Color c, int x_origin, int y_origin,
                int xRelative, int yRelative, int squareSize){
        this.erasable = erasable;
        this.Tvalue = Tvalue;
        this.x = x;
        this.y = y;
        this.c = c;
        this.x_origin = x_origin;
        this.y_origin = y_origin;
        //helps drawing the square
        this.xRelative = xRelative;
        this.yRelative = yRelative;
        this.squareSize = squareSize;
    }

    /** ABSTRACT METHODS **/

    // override in subclasses
    public abstract void draw(int squareSize, Graphics g);

    /** GETTER & SETTER METHODS **/

    /**
     * DELETE!
     * @param xStart x value of start positions
     * @param yStart y value of start position
     * @param xFinal x value of destination position
     * @param yFinal y value of destination position
     * this function sets the value of where the start and end destination is
     */
    public void setSF( int xStart, int yStart, int xFinal, int yFinal){}

    // override in PossiblePath
    public void setSCost(double value){}

    // override in PossiblePath
    public double determineSCost(Tile previousTile){
        System.out.println("ERROR! Cannot setSCost of a tile that is not of type 'PossiblePath' or 'Destinations'");
        return -1.0;
    }

    // override in PossiblePath
    public void setECost(){}

    // override in PossiblePath
    public void changeXYOrigin(int x, int y){}


    public int getDirection(Tile t){return - 1;}

    // returns the tile's y coordinate with respects to the grid
    public int getY(){ return y;}

    // returns the tile's x coordinate with respects to the grid
    public int getX(){ return x;}

    public double getTValue() {
        return SCost + ECost;
    }

    public boolean isErasable(){
        return erasable;
    }

    public void setTvalue(double Tvalue){
        this.Tvalue = Tvalue;
    }

    // returns the x value of the Tile that lead to the current tile
    public int getX_origin(){
        return x_origin;
    }

    // returns the x value of the Tile that lead to the current tile
    public int getY_origin(){
        return y_origin;
    }

    public void setColor(Color c){
        this.c = c;
    }

    //prints out the information on the tiLE
    @Override
    public String toString() {
        return "Erasable: " + erasable + "\n"
                +"TValue: " + getTValue() + "\n"
                +"x: " + x + "\n"
                +"y: " + y + "\n"
                +"Color: " + c + "\n"
                +"X_origin: " + x_origin + "\n"
                +"Y_origin: " + y_origin + "\n";
    }




}
