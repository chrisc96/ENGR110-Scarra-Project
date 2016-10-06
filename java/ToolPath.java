


/**
 * ToolPath stores motor contol signals (pwm)
 * and motor angles
 * for given drawing and arm configuration.
 * Arm hardware takes sequence of pwm values
 * to drive the motors
 * @Arthur Roberts
 * @1000000.0
 */
import ecs100.UIFileChooser;
import ecs100.UI;
import java.io.*;
import java.util.*;


public class ToolPath{
     int n_steps; //straight line segmentt will be broken
                      // into that many sections

     // storage for angles and
     // moto control signals
     ArrayList<Double> theta1_vector;
     ArrayList<Double> theta2_vector;
     ArrayList<Integer> pen_vector;
     ArrayList<Integer> pwm1_vector;
     ArrayList<Integer> pwm2_vector;
     ArrayList<Integer> pwm3_vector;

    /**
     * Constructor for objects of class ToolPath
     */
    public ToolPath()
    {
        // initialise instance variables
      n_steps = 1;
      theta1_vector = new ArrayList<Double>();
      theta2_vector = new ArrayList<Double>();
      pen_vector = new ArrayList<Integer>();
      pwm1_vector = new ArrayList<Integer>();
      pwm2_vector = new ArrayList<Integer>();
      pwm3_vector = new ArrayList<Integer>();

    }

    /**********CONVERT (X,Y) PATH into angles******************/
    public void convert_drawing_to_angles(Drawing drawing,Arm arm,String fname){

        // for all points of the drawing...
        for (int i = 1 ; i < drawing.get_drawing_size();i++){
            // take two points
            PointXY p0 = drawing.get_drawing_point(i-1);
            PointXY p1 = drawing.get_drawing_point(i);
            n_steps = (int) Math.hypot(p1.get_x() - p0.get_x(), p1.get_y() - p0.get_y());
            // break line between points into segments: n_steps of them
            for ( int j = 0 ; j< n_steps;j++) { // break segment into n_steps str. lines
                double x = p0.get_x() + j*(p1.get_x()-p0.get_x())/n_steps;
                double y = p0.get_y() + j*(p1.get_y()-p0.get_y())/n_steps;
                arm.inverseKinematic(x, y);
                theta1_vector.add(arm.get_theta1()*180/Math.PI);
                theta2_vector.add(arm.get_theta2()*180/Math.PI);
                pwm1_vector.add(arm.get_pwm1());
                pwm2_vector.add(arm.get_pwm2());
                if (p1.get_pen()) {
                	// Pen down is 1700 PWM
                	pwm3_vector.add(1650);
                }
                else {
                	// Pen up is 1200 PWM
                	pwm3_vector.add(1200);
                    break;
                }
            }
        }
    }

    // takes sequence of angles and converts it
    // into sequence of motor signals
    public void convert_angles_to_pwm(Arm arm){
        // for each angle
        for (int i=0 ; i < theta1_vector.size();i++){
            arm.set_angles(theta1_vector.get(i),theta2_vector.get(i));
            // pwm are not changing
            pwm1_vector.add(arm.get_pwm1());
            pwm2_vector.add(arm.get_pwm2());
        }
    }

    // save file with motor control values
    public void save_pwm_file(){
        String path = UIFileChooser.save("Save File");
	   if(path == null) return;
        File fname = new File(path);
        try {
            PrintStream out = new PrintStream(fname);
            for(int i=0; i< pwm1_vector.size(); i++){
                out.printf("%d,%d,%d\n", pwm1_vector.get(i), pwm2_vector.get(i), pwm3_vector.get(i));
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
