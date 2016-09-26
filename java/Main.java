 

/* Code for Assignment ?? 
 * Name:
 * Usercode:
 * ID:
 */


import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.*;


/** <description of class Main>
 */
public class Main{

    private Arm arm;
    private Drawing drawing;
    private ToolPath tool_path;
    // state of the GUI
    //-----Possibly replace with enum
    private int state; // 0 - nothing
                       // 1 - inverse point kinematics - point
                       // 2 - enter path. Each click adds point  
                       // 3 - enter path pause. Click does not add the point to the path
    
    /**      */
    public Main(){
        UI.initialise();
        UI.addButton("xy to angles", this::inverse);    //-----Sets state to 1 then calls arm.draw
        UI.addButton("Enter path XY", this::enter_path_xy); //-----Sets state to 2
        UI.addButton("Save path XY", this::save_xy);    //-----Sets state to 0 then opens UI.fileChooser.Save() saves file through the drawing object (drawing.save_path(fname))
        UI.addButton("Load path XY", this::load_xy);    //-----Sets state to 0 then loads a file through the drawing object (drawing.load_path(fname)) it then draws through arm and drawing object (drawing.draw, arm.draw)
        UI.addButton("Save path Ang", this::save_ang);  //-----Does something through the tool_path object, supposedly saves the angles tool_path.convert_drawing_to_angles(drawing,arm,fname);
        UI.addButton("Load path Ang:Play", this::load_ang); //-----Does nothing (yet)

        UI.addButton("Save PWM", this::savePWM);

       // UI.addButton("Quit", UI::quit);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);


        //ServerSocket serverSocket = new ServerSocket(22); 
        
        this.arm = new Arm();   //-----Initialize objects
        this.drawing = new Drawing();

        tool_path = new ToolPath();
        this.run();
        arm.draw(); //-----irrelevant - called inside run()
    }
    
    public void doKeys(String action){
        UI.printf("Key :%s \n", action);
        if (action.equals("b")) {
            // break - stop entering the lines
            state = 3;
            //
          
        }
               
    }
    
    
    public void doMouse(String action, double x, double y) {
         //UI.printf("Mouse Click:%s, state:%d  x:%3.1f  y:%3.1f\n",
         //   action,state,x,y);
        UI.clearGraphics();
        String out_str=String.format("%3.1f %3.1f",x,y);
        UI.drawString(out_str, x+10,y+10);
         // 
         if ((state == 1)&&(action.equals("clicked"))){
          // draw as 
          
          arm.inverseKinematic(x,y);
          arm.draw();
          return;
        }
        
         if ( ((state == 2)||(state == 3))&&action.equals("moved") ){
          // draw arm and path
          arm.inverseKinematic(x,y);
          arm.draw();
         
          // draw segment from last entered point to current mouse position
          if ((state == 2)&&(drawing.get_path_size()>0)){
            PointXY lp = new PointXY();
            lp = drawing.get_path_last_point();
            //if (lp.get_pen()){
               UI.setColor(Color.GRAY);
               UI.drawLine(lp.get_x(),lp.get_y(),x,y);
           // }
          }
           drawing.draw();
        }
        
        // add point
        if (   (state == 2) &&(action.equals("clicked"))){
            // add point(pen down) and draw
            UI.printf("Adding point x=%f y=%f\n",x,y);
            drawing.add_point_to_path(x,y,true); // add point with pen down
            
            arm.inverseKinematic(x,y);
            arm.draw();
            drawing.draw();
            drawing.print_path();
        }
        
        
        if (   (state == 3) &&(action.equals("clicked"))){
            // add point and draw
            //UI.printf("Adding point x=%f y=%f\n",x,y);
            drawing.add_point_to_path(x,y,false); // add point wit pen up
            
            arm.inverseKinematic(x,y);
            arm.draw();
            drawing.draw();
            drawing.print_path();
            state = 2;
        }
        
        
    }
   
    
    public void save_xy(){
        state = 0;
        String fname = UIFileChooser.save();
        drawing.save_path(fname);
    }
    
    public void enter_path_xy(){
         state = 2;
    }
    
    public void inverse(){
         state = 1;
         arm.draw();
    }
    
    public void load_xy(){
        state = 0;
        String fname = UIFileChooser.open();
        drawing.load_path(fname);
        drawing.draw();
        
        arm.draw();
    }
    
    // save angles into the file
    public void save_ang(){
        tool_path.convert_drawing_to_angles(drawing,arm);
    }
    
    
    public void load_ang(){
        
    }
    
    public void run() {
        while(true) {
            arm.draw();
            UI.sleep(20);
        }
    }

    private void savePWM(){
        tool_path.convert_drawing_to_angles(drawing,arm);
        tool_path.convert_angles_to_pwm(arm);
        tool_path.save_pwm_file();
    }

    public static void main(String[] args){
        Main obj = new Main();
    }    

}
