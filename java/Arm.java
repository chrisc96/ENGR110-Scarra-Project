


/**
 * Class represents SCARA robotic arm.
 *
 * @Arthur Roberts
 * @0.0
 */

import ecs100.UI;
import java.awt.Color;

public class Arm
{
    //------ Picture takes 640px X 480px
    // fixed arm parameters
    private final int motor1X = 290;  // coordinates of the motor(measured in pixels of the picture)
    private final int motor1Y = 372;
    private final int motor2X = 379;
    private final int motor2Y = 374;
    private final double r = 156.0;  // length of the upper/fore arm
    // parameters of servo motors - linear function pwm(angle)
    // each of two motors has unique function which should be measured
    // linear function cam be described by two points
    // motor 1, point1
    private double pwm1_val_1;
    private double theta1_val_1;
    // motor 1, point 2
    private double pwm1_val_2;
    private double theta1_val_2;

    // motor 2, point 1
    private double pwm2_val_1;
    private double theta2_val_1;
    // motor 2, point 2
    private double pwm2_val_2;
    private double theta2_val_2;










    // current state of the arm
    private double theta1; // angle of the upper arm
    private double theta2;
    private int pwm1;
    private int pwm2;

    private double joint1X;     // positions of the joints
    private double joint1Y;
    private double joint2X;
    private double joint2Y;
    private double toolX;     // position of the tool
    private double toolY;
    private boolean valid_state; // is state of the arm physically possible?

    /**
     * Constructor for objects of class Arm
     */
    public Arm()
    {
        theta1 = -90.0*Math.PI/180.0; // initial angles of the upper arms
        theta2 = -90.0*Math.PI/180.0;
        valid_state = false;
        pwm1 = 1500;
        pwm2 = 1500;
    }

    // draws arm on the canvas
    public void draw()
    {
        // draw arm
        int height = UI.getCanvasHeight();
        int width = UI.getCanvasWidth();
        // calculate joint positions
        joint1X = motor1X + r*Math.cos(theta1);
        joint1Y = motor1Y + r*Math.sin(theta1);
        joint2X = motor2X + r*Math.cos(theta2);
        joint2Y = motor2Y + r*Math.sin(theta2);

        //draw motors and write angles
        int mr = 20;
        UI.setLineWidth(5);
        UI.setColor(Color.BLUE);
        UI.drawOval(motor1X -mr/2, motor1Y -mr/2,mr,mr);
        UI.drawOval(motor2X -mr/2, motor2Y -mr/2,mr,mr);
        // write parameters of first motor
        String out_str=String.format("t1=%3.1f",theta1*180/Math.PI);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+2*mr);
        out_str=String.format("motor1X=%d", motor1X);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+3*mr);
        out_str=String.format("motor1Y=%d", motor1Y);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+4*mr);
        out_str=String.format("PWM1=%d", pwm1);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+5*mr);

        // ditto for second motor
        out_str = String.format("t2=%3.1f",theta2*180/Math.PI);
        UI.drawString(out_str, motor2X +2*mr, motor2Y -mr/2+2*mr);
        out_str=String.format("motor2X=%d", motor2X);
        UI.drawString(out_str, motor2X +2*mr, motor2Y -mr/2+3*mr);
        out_str=String.format("motor2Y=%d", motor2Y);
        UI.drawString(out_str, motor2X +2*mr, motor2Y -mr/2+4*mr);
        out_str=String.format("PWM2=%d", pwm2);
        UI.drawString(out_str, motor2X -2*mr, motor2Y -mr/2+5*mr);


    //    UI.drawString(("PWM " + get_pwm1()), motor1X -2*mr, -mr/2+5*mr);
    //    UI.drawString(("PWM " + get_pwm2()), motor2X -2*mr, -mr/2+5*mr);
    // parameters of servo motors - linear function pwm(angle)
    // each of two motors has un






        // draw Field Of View
        UI.setColor(Color.GRAY);
        UI.drawRect(0,0,640,480);

       // it can be uncommented later when
       // kinematic equations are derived
        if ( valid_state) {
          // draw upper arms
          UI.setColor(Color.GREEN);
          UI.drawLine(motor1X, motor1Y, joint1X, joint1Y);
          UI.drawLine(motor2X, motor2Y, joint2X, joint2Y);
          //draw forearms
          UI.drawLine(joint1X, joint1Y, toolX, toolY);
          UI.drawLine(joint2X, joint2Y, toolX, toolY);
          // draw tool
          double rt = 20;
          UI.drawOval(toolX -rt/2, toolY -rt/2,rt,rt);
        }

   }

   public double dist(double p1X, double p1Y, double p2X, double p2Y) {
       return Math.sqrt((p2X-p1X)*(p2X-p1X) + (p2Y-p1Y)*(p2Y-p1Y));
   }


   // calculate tool position from motor angles
   // updates variable in the class
   public void directKinematic(){

       double distance = dist(joint1X, joint1Y, joint2X, joint2Y);  //Distance between two points

       if(distance < 2*r){
           valid_state = true;
           double xA = (joint1X + joint2X)/2;
           double yA = (joint1Y + joint2Y)/2;

           double aLen = Math.sqrt(xA*xA + yA*yA);

           double h = dist(0, r, 0, aLen);
           double a = Math.atan((joint2Y-joint1Y)/(joint2X-joint1X));

           toolX = xA - h * Math.cos(a - Math.PI/2);
           toolY = yA - h * Math.sin(a - Math.PI/2);

       }else{
           valid_state = false;
       }

    /*
       // midpoint between jointsconvert_angles_to_pwm
       //double  xa =.... ;
       //double  ya =.... ;
       // distance between joints
       //double d = ...;
       if (d<2*r){
           valid_state = true;h
         // half distance between tool positions
         //double  h = ...;
         //double alpha= ...;
         // tool position
        // double toolX = ...;
        // double toolY = ...;
         //  xt2 = xa - h.*cos(alpha-pi/2);
         //  yt2 = ya - h.*sin(alpha-pi/2);
       } else {
           valid_state = false;
        }
      */
    }

    // motor angles from tool position
    // updetes variables of the class
    public void inverseKinematic(double toolXNew,double toolYNew){

        toolX = toolXNew;
        toolY = toolYNew;
        valid_state = true;

        // distance between pem and motor1
        double d1 = dist(toolX, toolY, motor1X, motor1Y);


        if (d1>2*r){
            UI.println("Arm 1 - can not reach");
            valid_state = false;
            return;
        }


        double xa = motor1X + 0.5*(toolX - motor1X);
        double ya = motor1Y + 0.5*(toolY - motor1Y);

        double l1 = d1/2;
        double h1 = Math.sqrt(r*r - l1*l1);
        double alpha = Math.PI/2.0 - Math.atan2(motor1Y - toolY,toolX - motor1X);

        // elbows positions
        joint1X = xa - h1 * Math.cos(alpha);
        joint1Y = ya - h1 * Math.sin(alpha);

        theta1 = Math.atan2(joint1Y - motor1Y, joint1X - motor1X);

        /*
        if ((theta1>0)||(theta1<-Math.PI)){
            valid_state = false;
            UI.println("Ange 1 -invalid");
            return;
        }
        */

        // theta12 = atan2(yj12 - motor1Y,xj12-motor1X);

        double d2 = dist(toolX, toolY, motor2X, motor2Y);


        if (d2>2*r){
            UI.println("Arm 2 - can not reach");
            valid_state = false;
            return;
        }


        double l2 = d2/2;
        double h2 = Math.sqrt(r*r - l2*l2);
        double alpha2 = Math.PI/2.0 - Math.atan2( motor2Y-toolY, toolX - motor2X);
        xa = motor2X + 0.5*(toolX - motor2X);
        ya = motor2Y + 0.5*(toolY - motor2Y);

        // elbows positions
        joint2X = xa + h2 * Math.cos(alpha2);
        joint2Y = ya + h2 * Math.sin(alpha2);

        // motor angles for both 1st elbow positions
        theta2 = Math.atan2(joint2Y - motor2Y, joint2X - motor2X);

        pwm1 = (int)((-10*theta1*180.0/Math.PI)+240);
        pwm2 = (int)((-10*theta2*180.0/Math.PI)+890);
        /*
        if ((theta2>0)||(theta2<-Math.PI)){
            valid_state = false;
            //UI.println("Ange 2 -invalid");
            return;
        }
        */


        UI.printf("toolX:%3.1f, toolY:%3.1f\n",toolX,toolY);
        UI.printf("theta1:%3.1f, theta2:%3.1f\n",theta1*180/Math.PI,theta2*180/Math.PI);
        return;

    }

    // returns angle of motor 1
    public double get_theta1(){
        return theta1;
    }
    // returns angle of motor 2
    public double get_theta2(){
        return theta2;
    }
    // sets angle of the motors
    public void set_angles(double t1, double t2){
        theta1 = t1;
        theta2 = t2;
    }

    // returns motor control signal
    // for motor to be in position(angle) theta1
    // linear interpolation
    public int get_pwm1(){
        return pwm1;
    }
    // ditto for motor 2
    public int get_pwm2(){
        return pwm2;
    }
 }
