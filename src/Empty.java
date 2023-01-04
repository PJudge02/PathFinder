import java.awt.*;

public class Empty extends Tile{
    private final int x;
    private final int y;
    private Color c = Color.white;
    // initialize variables
    public Empty(int x, int y, int squareSize){
        super(true, -1, x, y, Color.white, -1, -1, 341, 31, squareSize);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(int squareSize, Graphics g){
        g.setColor(c);
        g.fillRect(x*(squareSize + 2) + xRelative, y*(squareSize + 2) + yRelative, squareSize, squareSize);
    }

    /** TO SEE WHAT SQUARES ARE BEING CONSIDERED **/
    public void squareConsidered(){
        c = Color.red;
    }


}