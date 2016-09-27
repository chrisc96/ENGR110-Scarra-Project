

/* Code for Assignment 6
 * Name:
 * Usercode:
 * ID:
 */


import ecs100.*;
import java.awt.*;
import java.io.*;

/** <description of class Main>
 */
public class Main{

    // UI Width
    int width;
    int height;
    
	
    private Arm arm;
    private Drawing drawing;
    private ToolPath tool_path;

    // State of the GUI
    private int state; // 0 - nothing
                       // 1 - inverse point kinematics - point
                       // 2 - enter path. Each click adds point
                       // 3 - enter path pause. Click does not add the point to the path
    
    public Main(){
    	
    	UI.initialise();
    	
    	// Setting Window Size
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth()-200;
        height = (int) screenSize.getHeight()-200;
        UI.setWindowSize(width, height);
        UI.setDivider(0.4);

        UI.addButton("Enter path XY", this::enter_path_xy);
        UI.addButton("Generate Circle", this::generateCircle);
        UI.addButton("Convert to PWM", this::savePWM);
        UI.addButton("Send to RPi", this::sendToRPI);
        UI.addButton("Quit", UI::quit);

        
        // Event Handlers
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);

        // Instructions
        UI.println("Instructions");
        UI.println("---------------------");
        UI.println("Begin by clicking Enter Path XY, this will handle all");
        UI.println("singularities and issues relating to the two Servo Motors");
        UI.println("Once you have completed the diagram you wish to create, click");
        UI.println("Convert to PWM, this will ask you to save the .txt file. That's it!");
        UI.println("---------------------");
        UI.println("Useful Commands: ");
        UI.println("Press 'd' to begin drawing (or click the button to left)");
        UI.println("Press 'b' to lift up pen drawing");
        UI.println("Press 'c' to clear path of pen drawing");
        UI.println("Press 'q' to quit the program or click quit button");
        
        
        //-----Initialize objects
        this.arm = new Arm();   
        this.drawing = new Drawing();
        this.tool_path = new ToolPath();
        
        // Runs main program
        this.run();
    }

    public void doKeys(String action){
        UI.printf("Key :%s \n", action);
        if (action.equals("b")) {
            // break - stop entering the lines
            state = 3;
        }

        if (action.equals("q")) {
        	UI.quit();
        }
        
        if (action.equals("c")) {
        	drawing.getPath().clear();
        	UI.clearGraphics();
        }
        
        if (action.equals("d")) {
        	this.enter_path_xy();
        }
    }

    public void doMouse(String action, double x, double y) {
    	UI.clearGraphics();
        
        // draws x/y coords at cursor
        String out_str=String.format("(%3.1f, %3.1f)",x,y);
        UI.drawString(out_str, x,y-20);
        
        
        if ((state == 1) && (action.equals("clicked"))){
          arm.inverseKinematic(x,y);
          arm.draw();
          return;
        }

        if (((state == 2) || (state == 3)) && action.equals("moved") ){
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
    		}
        	drawing.draw();
        }

        // Adds point to path arraylist provided it isn't a singularity
        // defined by 'valid_state' boolean in Arm.
        if ((state == 2) &&(action.equals("clicked")) && Arm.valid_state){
            // add point(pen down) and draw
            UI.printf("Adding point x=%f y=%f\n",x,y);
            drawing.add_point_to_path(x,y,true); // add point with pen down

            arm.inverseKinematic(x,y);
            arm.draw();
            drawing.draw();
            drawing.print_path();
        }


        if ((state == 3) &&(action.equals("clicked")) && Arm.valid_state){
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

    public void generateCircle(){
        arm.circle(drawing);
    }

    public void enter_path_xy(){
    	UI.clearText();
        state = 2;
    }

    public void inverse(){
         state = 1;
         arm.draw();
    }
    
    public void run() {
        while(true) {
            arm.draw();
            UI.sleep(20);
        }
    }

    private void savePWM(){
    	String fname = " ";
        tool_path.convert_drawing_to_angles(drawing,arm,fname);
        tool_path.save_pwm_file();
    }

    public void sendToRPI() {
        String path = UIFileChooser.open();

        try {
            String command = "expect /am/rialto/home1/tanoielis/private/ENGR110/SCARA/transfer.exp " + path;
            Process proc = Runtime.getRuntime().exec(command);


            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }
            proc.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    
    public static void main(String[] args){
        new Main();
    }
}
