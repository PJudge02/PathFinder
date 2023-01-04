import java.awt.*;

public class Destinations extends Tile{
    private final int x;
    private final int y;

    // initialize variables
    public Destinations(int x, int y, int squareSize){ //Destination has a T-value of -2, since it is the lowest square that still has meaningful value
        super(true, -2, x, y, Color.red, -1,-1, 341, 31, squareSize);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(int squareSize, Graphics g){
        g.setColor(Color.red);
        g.fillRect(x*(squareSize + 2) + xRelative, y*(squareSize + 2) + yRelative, squareSize, squareSize);


    }

}