import java.awt.*;

public class Wall extends Tile{
    private final int x;
    private final int y;

    // initialize variables
    public Wall(int x, int y, int squareSize){
        super(true, -1, x, y, Color.gray, -1,-1, 341, 31, squareSize);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(int squareSize, Graphics g){
        g.setColor(new Color(73,73,73));
        g.fillRect(x*(squareSize + 2) + xRelative, y*(squareSize + 2) + yRelative, squareSize, squareSize);
    }

}
