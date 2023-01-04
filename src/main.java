import java.awt.Container;
import java.util.Scanner;

import javax.swing.JFrame;

public class main {

    private static JFrame frame;
    private static Board board;
    private static Container container;

    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);
        System.out.println("What size would you like your board?");
        boolean worked = false;
        int size=5;
        while(!worked) {
            worked=true;
            try {
                size = Integer.parseInt(sc.nextLine());
                if(size<1 || size > 200){
                    System.out.println("Size must be a number between 1 and 200, inclusive.");
                    System.out.println("What size would you like your board?");
                    worked = false;
                }
            } catch (Exception e) {
                System.out.println("Size must be a number between 1 and 200, inclusive.");
                System.out.println("What size would you like your board?");
                worked = false;
            }
        }

        board = new Board(size); //1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 24, 25, 30, 40, 50, 60, 75, 100, 120, 150, 200

        frame = new JFrame();
        frame.setTitle("Path Finder");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container = frame.getContentPane();
        container.add(board);


        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

    }
}
