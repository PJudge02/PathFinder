import java.awt.*;

public class PossiblePath extends Tile{
    // x & y location of this tile
    private final int x;
    private final int y;

    public int xStart, yStart, xFinal, yFinal;

    // initialize variables
    public PossiblePath(int x, int y, int x_origin, int y_origin, int squareSize){
        super(true, -1, x, y, Color.yellow, x_origin, y_origin, 341, 31, squareSize);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(int squareSize, Graphics g){
        g.setColor(c);
        g.fillRect(x*(squareSize + 2) + xRelative, y*(squareSize + 2) + yRelative, squareSize, squareSize);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("" + Math.round(getTValue() * 100) / 100.0, x * (squareSize + 2) + xRelative + squareSize / 7, y * (squareSize + 2) + yRelative + squareSize * 3 / 5);
    }

    @Override
    public void setSF( int xStart, int yStart, int xFinal, int yFinal) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.xFinal = xFinal;
        this.yFinal = yFinal;
    }

    /**
     *
     * @param previousTile - this HAS to be either PossiblePath tile or Destination tile
     */
    @Override
    public double determineSCost(Tile previousTile){
        if (previousTile instanceof PossiblePath){
            PossiblePath p = (PossiblePath) previousTile;
            return p.getSCost() +
                    Math.sqrt(Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2));
        } else if(previousTile instanceof Destinations){
            return Math.sqrt(Math.pow(x - previousTile.getX(), 2) + Math.pow(y - previousTile.getY(), 2));
        }else{
            System.out.println("ERROR! Cannot setSCost of a tile that is not of type 'PossiblePath' or 'Destinations'");
            return -1;
        }
    }
    @Override
    public void setSCost(double value){
        SCost = value;
    }

    /** DETERMINES DISTANCE TO THE END GOAL GIVEN ONLY DIAGONAL, VERTICAL, AND HORIZONTAL DIRECTION **/
    @Override
    public void setECost(){
        int xTemp = x, yTemp = y;
        int diagonalCount = 0;
        int straightCount = 0;

        while (xTemp != xFinal || yTemp != yFinal){
            /** CHECKS DIAGONAL DIRECTION **/
            if (xFinal > xTemp && yFinal < yTemp){ /** CHECK UP & RIGHT **/
                diagonalCount++;
                // moves the locational "square" up and to the right
                yTemp--;
                xTemp++;
            }else if (xFinal > xTemp && yFinal > yTemp){ /** CHECK DOWN & RIGHT **/
                diagonalCount++;
                // moves the locational "square" down and to the right
                yTemp++;
                xTemp++;
            }else if (xFinal < xTemp && yFinal < yTemp){ /** CHECK UP & LEFT **/
                diagonalCount++;
                // moves the locational "square" up and to the left
                yTemp--;
                xTemp--;
            }else if (xFinal < xTemp && yFinal > yTemp){ /** CHECK DOWN & LEFT **/
                diagonalCount++;
                // moves the locational "square" down and to the left
                yTemp++;
                xTemp--;
            }

            /** CHECKS VERTICAL & HORIZONTAL DIRECTION **/
            if ((xFinal > xTemp || xFinal < xTemp) && yFinal == yTemp){
                straightCount = Math.abs(xFinal - xTemp);
                break;
            }else if((yFinal > yTemp || yFinal < yTemp) && xFinal == xTemp){
                straightCount = Math.abs((yFinal - yTemp));
                break;
            }
        }
        ECost =  Math.sqrt(2) * ((double) diagonalCount) + straightCount;
    }

    @Override
    public void changeXYOrigin(int x, int y){
        x_origin = x;
        y_origin = y;
    }

    public double getSCost(){
        return SCost;
    }

    private double getECost(){
        return ECost;
    }

    @Override
    public double getTValue() {
        return SCost + ECost;
    }
}