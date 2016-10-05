


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

    // angle of the upper arm (radians)
    private double theta1; // LHS motor
    private double theta2; // RHS motor
    
    // pulse width modulation values for servo motor 
    // to achieve the above theta's
    private int pwm1;
    private int pwm2;

    // positions of the joints
    private double joint1X;
    private double joint1Y;
    private double joint2X;
    private double joint2Y;
    
    // Distance between the two joints - used for check for singularity
    // Shouldn't ever be above 312 (2 * length of upper arm)
    private double d1;
    
    // position of the tool
    private double toolX;
    private double toolY;
    
    public static boolean valid_state; // is state of the arm physically possible?
    
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

        // LHS Motor Parameters (Theta, PWM etc)
        String out_str=String.format("Angle 1: %3.1f",theta1*180/Math.PI);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+2*mr);
        
        out_str=String.format("motor1X: %d", motor1X);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+3*mr);
        
        out_str=String.format("motor1Y: %d", motor1Y);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+4*mr);
        
        out_str=String.format("PWM1: %d", pwm1);
        UI.drawString(out_str, motor1X -2*mr, motor1Y -mr/2+5*mr);
        
        
        // RHS Motor Parameters (Theta, PWM etc)
        out_str = String.format("Angle 2: %3.1f",theta2*180/Math.PI);
        UI.drawString(out_str, motor2X -2*mr, motor2Y -mr/2+2*mr);
        
        out_str=String.format("motor2X: %d", motor2X);
        UI.drawString(out_str, motor2X -2*mr, motor2Y -mr/2+3*mr);
        
        out_str=String.format("motor2Y: %d", motor2Y);
        UI.drawString(out_str, motor2X -1002*mr, motor2Y -mr/2+4*mr);
        
        out_str=String.format("PWM2: %d", pwm2);
        UI.drawString(out_str, motor2X -2*mr, motor2Y -mr/2+5*mr);
        

        // draw Field Of View
        UI.setColor(Color.GRAY);
        UI.drawRect(0,0,640,480);

        if (valid_state) {
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

    // Pythagoras Theorem from direct Kinematics
   public double dist(double p1X, double p1Y, double p2X, double p2Y) {
       return Math.sqrt((p2X-p1X)*(p2X-p1X) + (p2Y-p1Y)*(p2Y-p1Y));
   }


   // calculate tool position from motor angles
   // updates variable in the class
   public void directKinematic(){

	   double distance = dist(joint1X, joint1Y, joint2X, joint2Y);  // Distance between two points

       // If distance between two joints > 
       if(distance < 2*r){
           valid_state = true;
           double xA = (joint1X + joint2X)/2;
           double yA = (joint1Y + joint2Y)/2;

           //double aLen = Math.sqrt(xA*xA + yA*yA);
           //double h = dist(0, r, 0, aLen);
	       
	   double h = Math.sqrt(r*r + Math.sqrt(xA*xA + yA*yA));
	       
           double a = Math.atan((joint2Y-joint1Y)/(joint2X-joint1X));

           toolX = xA - h * Math.cos(a - Math.PI/2);
           toolY = yA - h * Math.sin(a - Math.PI/2);
       }
       else {
           valid_state = false;
       }
    }

    // motor angles from tool position
    // updetes variables of the class
    public void inverseKinematic(double toolXNew, double toolYNew){

        toolX = toolXNew;
        toolY = toolYNew;
        valid_state = true;

        // --- CALCULATE THETA 1 (LHS)
        
        // distance between pen and motor1
        d1 = dist(toolX, toolY, motor1X, motor1Y);

        // Singularity check for arm length - Arm 2
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

        // Elbow positions
        joint1X = xa - h1 * Math.cos(alpha);
        joint1Y = ya - h1 * Math.sin(alpha);

        // Calculates angle required of LHS servo to achieve current x/y coord from J1
        theta1 = Math.atan2(joint1Y - motor1Y, joint1X - motor1X);
        
        // Singularity check for moving arms too far
        if ((theta1>0)||(theta1< -Math.PI)){
            valid_state = false;
            UI.println("Angle 1 - invalid");
            return;
        }
        
        // --- END CALCULATION THETA 1 (LHS)
        
        // --- BEGIN CALCULATION THETA 2 (RHS)

        double d2 = dist(toolX, toolY, motor2X, motor2Y);

        // Singularity check for arm length - Arm 2
        if (d2>2*r){
            UI.println("Arm 2 - can not reach");
            valid_state = false;
            return;
        }

        double l2 = d2/2;
        double h2 = Math.sqrt(r*r - l2*l2);
        double alpha2 = Math.PI/2.0 - Math.atan2(motor2Y-toolY, toolX - motor2X);
        
        xa = motor2X + 0.5*(toolX - motor2X);
        ya = motor2Y + 0.5*(toolY - motor2Y);

        // Elbow positions
        joint2X = xa + h2 * Math.cos(alpha2);
        joint2Y = ya + h2 * Math.sin(alpha2);

        // Calculates angle required of LHS servo to achieve current x/y coord from J2
        theta2 = Math.atan2(joint2Y - motor2Y, joint2X - motor2X);

        // Singularity check for moving arms too far
        if ((theta2>0)||(theta2<-Math.PI)){
            valid_state = false;
            UI.println("Angle 2 - invalid");
            return;
        }
        
        //--- END CALCULATION THETA 1 (RHS)
        
        // Calculate PWM from angle's of motor
        
        // Theta is in radians - converted to degrees
        pwm1 = (int)((-10*theta1*180.0/Math.PI)+240);
        pwm2 = (int)((-10*theta2*180.0/Math.PI)+890);

        if (pwm1 > 2000 || pwm1 < 1000 || pwm2 > 2000 || pwm2 < 1000) {
            valid_state = false;
            UI.println("Out of bounds of Servo's - invalid");
            return;
        }

        if(Math.hypot(joint2X-joint1X, joint2Y-joint1Y) > 0.99*2*r || toolY > 265){
            valid_state = false;
            return;
        }

        UI.printf("toolX:%3.1f, toolY:%3.1f\n",toolX,toolY);
        UI.printf("theta1:%3.1f, theta2:%3.1f\n",theta1*180/Math.PI,theta2*180/Math.PI);
        return;
    }



    public void circle(Drawing drawing){
        drawing.getPath().clear();
        //(x – h)^2 + (y – k)^2 = r^2

        //r is radius
        //h is circle centre x
        //k is circle centre y

        double r = 100/3; //50 mm this needs to be changed when we find out pixel to mm ratio
        double h = motor1X + ((motor2X - motor1X)/2);
        double k = 170; //Arbitrary right now

        double x;
        double y;

        //int points = 50;
        for(int i=0; i<360; i++){
            x = r * Math.cos(i*Math.PI/180) + h;
            y = r * Math.sin(i*Math.PI/180) + k;
            drawing.add_point_to_path(x, y, true);
        }
        drawing.draw();

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

    // returns PWM values for LHS/RHS servo's (in degrees!)
    // calculated using linear interpolation
    public int get_pwm1(){
        return pwm1;
    }
    
    public int get_pwm2(){
        return pwm2;
    }
 }
