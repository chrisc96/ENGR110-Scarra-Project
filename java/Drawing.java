/**
 * Class represents the drawing as set of (x,y) points.
 *
 * @author (your name)
 * @version (a version number or a date)
 */

import ecs100.UI;
import java.util.*;

import java.awt.Color;


public class Drawing {

    // set of points
    private ArrayList<PointXY> path;

    /**
     * Constructor for objects of class Drawing
     */
    public Drawing()
    {
       path = new ArrayList<PointXY>();
    }

    public void add_point_to_path(double x, double y,boolean pen)
    {
        PointXY new_point = new PointXY(x,y,pen);
        path.add(new_point);
        UI.printf("Point added.x=%f y=%f pen=%b New path size - %d\n",
              x,y,pen,path.size());
    }

    public void print_path(){
        UI.printf("*************************\n");
        for (int i = 0; i < path.size();i++){

            double x0 = path.get(i).get_x();
            double y0 = path.get(i).get_y();
            boolean p = path.get(i).get_pen();
            UI.printf("i=%d x=%f y=%f pen=%b\n",i,x0,y0,p);
        }
        UI.printf("*************************\n");
    }

    public void draw(){
       //draw path
        for (int i = 1; i < path.size() ; i++){
            PointXY p0 = get_drawing_point(i-1);
            PointXY p1 = get_drawing_point(i);
            if (path.get(i).get_pen()){
                UI.setColor(Color.BLUE); //pen down part
            } else {
                UI.setColor(Color.LIGHT_GRAY); // pen uo
            }
            UI.drawLine(p0.get_x(), p0.get_y(), p1.get_x(), p1.get_y());

        }
    }

    // Getters and Setters
    
    public ArrayList<PointXY> getPath() {
    	return path;
    }
    
    public int get_path_size(){
        return path.size();
    }

    //pen_down = false for last point
    public void path_raise_pen(){
        path.get(path.size()-1).set_pen(false);
    }

    public PointXY get_path_last_point(){
        PointXY lp = path.get(path.size()-1);
        return lp;
    }

    public int get_drawing_size() {
        return path.size();
    }

    public PointXY get_drawing_point(int i){
        PointXY p = path.get(i);
        return p;
    }
}