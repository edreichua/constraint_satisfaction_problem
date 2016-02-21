package csp_solver;

import java.lang.reflect.Array;
import java.util.*;
import javafx.util.Pair;

/**
 * Created by edreichua on 2/15/16.
 */

/**
 * Assumption: 1) Dimensions less than 10000
 *             2) Fewer than 10 components (for Acscii graphics to look nice since components are printed as numbers,
 *                      can be tweaked if necessary)
 *
 */

public class CircuitBoard {

    private int boardWidth, boardHeight;
    private Map<Integer,Board> components;
    private static final int maxSize = 10000; // designed to ensure that integer is not overflowed
    private ConstraintSatisfactionProblem solver = new ConstraintSatisfactionProblem();

    public static class Board{
        protected int width, height, index;

        public Board(int width, int height, int index){
            this.width = width;
            this.height = height;
            this.index = index;
        }
    }

    public CircuitBoard(Board mainBoard, Map<Integer,Board> components){

        boardWidth = mainBoard.width;
        boardHeight = mainBoard.height;
        this.components  = components;

        // Create Variables and domains
        for(Map.Entry<Integer,Board> e: components.entrySet()){
            Board b = e.getValue();
            Set<Integer> domain = new HashSet<>();
            for(int x=0; x<=boardWidth-b.width; x++){
                for(int y=0; y<=boardHeight-b.height; y++){
                    domain.add(hashFunction(x,y));
                }
            }
            solver.addVariable(b.index,domain);
        }

        // Create constraints
        for(int i=1; i<=components.size(); i++){
            for(int j=1; j<=components.size(); j++){
                if(i==j)
                    continue;
                Set<Pair<Integer, Integer>> constraint = new HashSet<>();
                int w1 = components.get(i).width;
                int h1 = components.get(i).height;
                int w2 = components.get(j).width;
                int h2 = components.get(j).height;

                // loop through all combinations
                for(int x1=0; x1<=boardWidth-w1; x1++){
                    for(int y1=0; y1<=boardHeight-h1; y1++){
                        for(int x2=0; x2<=boardWidth-w2; x2++){
                            for(int y2=0; y2<=boardHeight-h2; y2++){
                                if(notOverlap(x1,y1,w1,h1,x2,y2,w2,h2)){
                                    constraint.add(new Pair<>(hashFunction(x1,y1),hashFunction(x2,y2)));
                                }
                            }
                        }
                    }
                }
                solver.addConstraint(components.get(i).index, components.get(j).index, constraint);
            }
        }

    }

    public int[][] solve() {
        Map<Integer, Integer> solution = solver.solve();
        if (solution == null)
            return null;
        int[][] result = new int[boardHeight][boardWidth];
        for(Map.Entry<Integer,Integer> e: solution.entrySet()){
            int index = e.getKey();
            Pair<Integer,Integer> p = reverseHash(e.getValue());
            int xlower = p.getKey();
            int ylower = p.getValue();
            int w = components.get(index).width;
            int h = components.get(index).height;

            for(int x=xlower; x<xlower+w; x++){
                for(int y=ylower; y<ylower+h; y++){
                    result[y2row(y)][x2col(x)] = index;
                }
            }

        }
        return result;
    }

    private boolean notOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){

        // check if the lower left of second is within the first
        if((x2>=x1) && (y2>=y1) && (x2<x1+w1) && (y2<y1+h1))
            return false;

        // check if the lower right of the second is within the first
        if((x2+w2>x1) && (y2>=y1) && (x2<x1+w1) && (y2<y1+h1))
            return false;

        // check if the upper left of the second is within the first
        if((x2>=x1) && (y2+h2>y1) && (x2<x1+w1) && (y2<y1+h1))
            return false;

        // check if the upper right of the second is within the first
        if((x2+w2>x1) && (y2+h2>y1) && (x2<x1+w1) && (y2<y1+h1))
            return false;

        return true;
    }


    private int hashFunction(int x, int y) {
        if( x>maxSize || y>maxSize ){
            System.err.println("Dimensions exceed maximum size. Integer overflow!");
            return Integer.MIN_VALUE; // error code
        }else{
            return x + y*maxSize;
        }
    }

    private Pair<Integer,Integer> reverseHash(int num){
        if( num<0 ){
            System.err.println("Dimensions exceed maximum size. Integer overflow!");
            return null;
        }else{
            return new Pair<>(num%maxSize,num/maxSize);
        }
    }

    private static void printBoard(int[][] res){
        for(int r=0; r<res.length; r++){
            for(int c=0; c<res[0].length; c++){
                if(res[r][c]==0)
                    System.out.print(". ");
                else
                    System.out.print(res[r][c]+" ");
            }
            System.out.println(" ");
        }
    }

    private int y2row(int y){
        return boardHeight - 1 - y;
    }

    private int x2col(int x){
        return x;
    }

    public static void main(String[] args){

        // Easy Board
        Board mainBoardEasy = new Board(10,3,0);
        Map<Integer,Board> componentEasy = new HashMap<>();
        componentEasy.put(1,new Board(3,2,1));
        componentEasy.put(2,new Board(5,2,2));
        componentEasy.put(3,new Board(2,3,3));
        componentEasy.put(4,new Board(7,1,4));

        // Medium Board
        Board mainBoardMedium = new Board(10,10,0);
        Map<Integer,Board> componentMedium = new HashMap<>();
        componentMedium.put(1,new Board(3,2,1));
        componentMedium.put(2,new Board(5,2,2));
        componentMedium.put(3,new Board(2,3,3));
        componentMedium.put(4,new Board(7,1,4));
        componentMedium.put(5,new Board(5,5,5));
        componentMedium.put(6,new Board(2,5,6));
        componentMedium.put(7,new Board(8,3,7));
        componentMedium.put(8,new Board(3,2,8));

        // Hard Board
        Board mainBoardHard = new Board(20,20,0);
        Map<Integer,Board> componentHard = new HashMap<>();
        componentHard.put(1,new Board(16,4,1));
        componentHard.put(2,new Board(16,4,2));
        componentHard.put(3,new Board(4,16,3));
        componentHard.put(4,new Board(4,16,4));
        componentHard.put(5,new Board(8,4,5));
        componentHard.put(6,new Board(8,4,6));
        componentHard.put(7,new Board(4,8,7));
        componentHard.put(8,new Board(4,8,8));
        componentHard.put(9,new Board(4,4,9));

        //int[][] solution = new CircuitBoard(mainBoardEasy,componentEasy).solve();
        //int[][] solution = new CircuitBoard(mainBoardMedium,componentMedium).solve();
        int[][] solution = new CircuitBoard(mainBoardHard,componentHard).solve();

        if (solution == null)
            System.out.println("Solution not found");
        else
            printBoard(solution);
    }

}
