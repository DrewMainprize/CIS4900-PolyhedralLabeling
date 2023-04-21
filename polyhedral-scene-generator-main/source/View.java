//  Copyright (c) 2015 Stephen Voland

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
//	associated documentation files (the "Software"), to deal in the Software without restriction, including 
//	without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
//	copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
//	following conditions:

//  The above copyright notice and this permission notice shall be included in all copies or substantial 
//	portions of the Software.

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
//	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
//	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
//	WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
//	SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package poly_package;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import poly_package.Conglomerate;
import poly_package.Point;
import poly_package.Polygon;
import poly_package.Main;

import com.jogamp.opengl.util.Animator;

// This class provides a dialog for the user to enter parameters and handles the view of the polygonal scene.
// It's an observer of the model in lieu of a controller class.
public class View implements ActionListener, Observer, GLEventListener, KeyListener
{
	Model my_model;
	
	// This holds the conglomerates (and thus all model data) once Model has finished generating the scene.
	List<Conglomerate> conglomerates = new ArrayList<Conglomerate>();
   
    Quaternion my_quaternion;
    DoubleBuffer rotation_matrix;
    
    GLU glu = new GLU();
 
    GLCanvas canvas = new GLCanvas();
 
    Frame scene_frame = new Frame("");

    Animator animator = new Animator(canvas);
    
	int number_of_planes;
	double poly_probability;
	String output_filename;
	boolean data_is_valid = false;
	
	// The following are elements of the input dialog.
	JFrame frame;

	JLabel planes_label;
	JLabel prob_label;
	JLabel filename_label;
		
	JTextField planes_text_field;
	JTextField prob_text_field;
	JTextField filename_text_field;
	
	JButton ok_button;

	JLabel planes_error;
	JLabel prob_error;
	JLabel filename_error;

	public View(int number_of_planes, double poly_probability, String output_filename, Model my_model) 
	{
		my_quaternion = new Quaternion();
		
		// Matrix representing no rotation and camera distance of -5 units from the origin along the z-axis. 
		double[] temp_array = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, -5, 1};
		rotation_matrix = DoubleBuffer.wrap(temp_array);
		
		// Setup for input dialog with default values.
		frame = new JFrame();
		this.number_of_planes = number_of_planes;
		this.poly_probability = poly_probability;
		this.output_filename = output_filename;
		this.my_model = my_model;

		planes_label = new JLabel("Number of Planes:");
		prob_label = new JLabel("Probability each Polyhedron is Visible:");
		filename_label = new JLabel("Filename to Save to:");
			
		planes_text_field = new JTextField(Integer.toString(number_of_planes), 20);
		planes_text_field.setName("planes");
		prob_text_field = new JTextField(Double.toString(poly_probability), 20);
		prob_text_field.setName("probabilities");
		filename_text_field = new JTextField(output_filename, 20);
		filename_text_field.setName("filename");
		ok_button = new JButton("Ok");
		ok_button.setName("ok_button");
		
		planes_error = new JLabel("");
		prob_error = new JLabel("");
		filename_error = new JLabel("");
		
		final View current = this;
		
		// Must only update GUI from EventQueue
		java.awt.EventQueue.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
		    	current.initialize_frame();
		    }
		});
	}
	
	public int getNumPlanes()
	{
		return Integer.parseInt(planes_text_field.getText());
	}

	public double getProbPolyhedronVisible()
	{
		return Double.parseDouble(prob_text_field.getText());
	}

	public String getFilename()
	{
		return filename_text_field.getText();
	}

	public void register(ActionListener caller)
	{
		ok_button.addActionListener(caller);
	}
	
	// This function creates the user input dialog.
	public void initialize_frame()
	{
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
			
		// Describe the dialog horizontally.
		GroupLayout.SequentialGroup h_group = layout.createSequentialGroup();
			
		GroupLayout.ParallelGroup temp_para = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		temp_para.addComponent(planes_label);
		temp_para.addComponent(prob_label);
		temp_para.addComponent(filename_label);
		h_group.addGroup(temp_para);
		
		temp_para = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		temp_para.addComponent(planes_text_field);
		temp_para.addComponent(prob_text_field);
		temp_para.addComponent(filename_text_field);
		temp_para.addComponent(ok_button);
		h_group.addGroup(temp_para);

		// These won't actually show up unless the user attempts to submit an invalid parameter.
		temp_para = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		temp_para.addComponent(planes_error);
		temp_para.addComponent(prob_error);
		temp_para.addComponent(filename_error);
		h_group.addGroup(temp_para);
		
		layout.setHorizontalGroup(h_group);
			
		// Describe the dialog vertically.
		GroupLayout.SequentialGroup v_group = layout.createSequentialGroup();

		temp_para = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		temp_para.addComponent(planes_label);
		temp_para.addComponent(planes_text_field);
		temp_para.addComponent(planes_error);
		v_group.addGroup(temp_para);

		temp_para = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		temp_para.addComponent(prob_label);
		temp_para.addComponent(prob_text_field);
		temp_para.addComponent(prob_error);
		v_group.addGroup(temp_para);

		temp_para = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		temp_para.addComponent(filename_label);
		temp_para.addComponent(filename_text_field);
		temp_para.addComponent(filename_error);
		v_group.addGroup(temp_para);

		temp_para = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		temp_para.addComponent(ok_button);
		v_group.addGroup(temp_para);

		layout.setVerticalGroup(v_group);
			
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
		register(this);
	}
	
	// This function gets and displays the polyhedral scene once Model finishes generating it.
	public void update(Observable obs, Object obj) 
	{
		if (obj instanceof List<?>)
		{
			this.conglomerates = (List<Conglomerate>)obj;
			
			// Create display window
	        canvas.addGLEventListener(this);
	        scene_frame.add(canvas);
	        scene_frame.setSize(640, 480);
	        scene_frame.setUndecorated(true);
	        scene_frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	        
	        // Anonymous function to handle dialog closing.
	        scene_frame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                exit();
	            }
	        });
	        
	        scene_frame.setVisible(true);
	        animator.start();
	        canvas.requestFocus();
		}
	}
	
	// This function is called when the user clicks the "Ok" button on the input dialog.
	// It validates the parameters and passes them on to Model if they're valid; otherwise it displays an error message
	//  in the appropriate error field.
	public void actionPerformed(ActionEvent event) 
    {
    	if (event.getSource() instanceof JButton)
    	{
    		JButton pressed_button = (JButton)event.getSource();
    		
    		// Validate input
    		if (pressed_button.getName() == "ok_button")
    		{
    			data_is_valid = true;
    			
    			planes_error.setText("");
    			prob_error.setText("");
    			filename_error.setText("");
    			
				if (!Main.isPositiveInteger(planes_text_field.getText()))
				{
					planes_error.setText("Number of planes must be a positive integer");
					data_is_valid = false;
				}

				if (!Main.isDouble(prob_text_field.getText()))
				{
					prob_error.setText("Probability must be a number between 0 and 1");
					data_is_valid = false;
				}
				else if (Double.parseDouble(prob_text_field.getText()) < 0 || Double.parseDouble(prob_text_field.getText()) > 1) 
				{
					prob_error.setText("Probability must be a number between 0 and 1");
					data_is_valid = false;
				} 

				if (filename_text_field.getText().isEmpty())
				{
					filename_error.setText("Output filename is required");
					data_is_valid = false;
				}
				
    			frame.pack();
    			
    			if (data_is_valid)
    			{
    				// Close dialog and tell Model to create scene
    	    		number_of_planes = Integer.parseInt(planes_text_field.getText());
    	    		poly_probability = Double.parseDouble(prob_text_field.getText());
    	    		output_filename = filename_text_field.getText().trim();
    	    		
    	    		// Add ".obj" to the end of the filename if it isn't there.
    	    		int filename_length = output_filename.length();
    	    		if (filename_length < 5 || !output_filename.endsWith(".obj"))
    	    		{
    	    			output_filename = output_filename.concat(".obj");
    	    		}

    				frame.setVisible(false);
    				frame.dispose();
    				my_model.create_scene(number_of_planes, poly_probability, output_filename);
    			}
    		}
    	}
    }
    	
	// This function watches for keystrokes while the scene is being displayed, to rotate the scene and exit the program.
	public void keyPressed(KeyEvent e) 
    {
//    	System.out.println("keyPressed" + e.getKeyCode());
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
    	{
    		exit();
    	}
    	if (e.getKeyCode() == KeyEvent.VK_UP)
    	{
    		rotation_matrix = my_quaternion.rotate("up");
    	}
    	if (e.getKeyCode() == KeyEvent.VK_DOWN)
    	{
    		rotation_matrix = my_quaternion.rotate("down");
    	}
    	if (e.getKeyCode() == KeyEvent.VK_LEFT)
    	{
    		rotation_matrix = my_quaternion.rotate("left");
    	}
    	if (e.getKeyCode() == KeyEvent.VK_RIGHT)
    	{
    		rotation_matrix = my_quaternion.rotate("right");
    	}
    }
    	 
	// Needed to implement KeyListener
	public void keyReleased(KeyEvent e) 
    {
    }
    	 
	// Needed to implement KeyListener
    public void keyTyped(KeyEvent e) 
    {
    }
    	    
    // This function actually draws the scene.
    public void display(GLAutoDrawable gLDrawable) 
    {
    	// Set up the OpenGL object for drawing.
        final GL2 gl = gLDrawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);		// All changes should be to the modelview matrix
        gl.glLoadIdentity();
        gl.glLoadMatrixd(rotation_matrix);				// Applies the cumulative effect of all rotations to the model
 
        // Draws a square around the scene to help with debugging the display and rotation.
//        gl.glBegin(GL2.GL_LINE_LOOP);               
//            gl.glColor3f(0.0f, 1.0f, 1.0f);   // set the color
//            gl.glVertex3f(-1.0f, 1.0f, 0.0f);   // Top Left
//            gl.glVertex3f( 1.0f, 1.0f, 0.0f);   // Top Right
//            gl.glVertex3f( 1.0f,-1.0f, 0.0f);   // Bottom Right
//            gl.glVertex3f(-1.0f,-1.0f, 0.0f);   // Bottom Left
//        gl.glEnd();                                                     

        // Offset the polygons so the outlines will be slightly on top and thus visible.
        gl.glPolygonOffset(1.0f, 1.0f);
        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        
        // Iterate through conglomerates, and within each iterate through draw_list to get polygons to draw
        for (Conglomerate conglo: conglomerates)
        {
        	List<Polygon> drawables = conglo.get_draw_list();

        	// Iterate through chosen polygons and draw each
            for (Polygon gon: drawables)
            {
                gl.glBegin(GL2.GL_POLYGON);
                gl.glColor3f(0.0f, 0.0f, 1.0f);   // Set the color
                
                // This does the actual drawing.
                for (Point current_point: gon.points)
                {
                    gl.glVertex3f((float)current_point.x_point, (float)current_point.y_point, (float)current_point.z_point);
                }
                gl.glEnd();                                                     
            }
        }
                
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);

        // Iterate through conglomerates, and within each iterate through outline_draw_list to get outlines of polygons to draw
        for (Conglomerate conglo: conglomerates)
        {
        	List<Polygon> drawables = conglo.get_outline_draw_list();
        	for (Polygon gon: drawables)
        	{
                gl.glBegin(GL2.GL_LINE_LOOP);               
                gl.glColor3f(1.0f, 1.0f, 1.0f);   // set the color
                
                // This does the actual drawing.
                for (Point current_point: gon.points)
                {
                    gl.glVertex3f((float)current_point.x_point, (float)current_point.y_point, (float)current_point.z_point);
                }
                gl.glEnd();                                                     
        	}
        }
        
        // This was used during debugging to draw all polygon lines, including lines which should not be visible due to merging.
        // To add it back you would need a global boolean called toggle_on and a key to change it in keyPressed().
        // If toggle is active, draw chosen polygons
//        if (toggle_on)
//        {
//        	for (Polygon gon: chosen_polygons)
//        	{
//                gl.glBegin(GL2.GL_LINE_LOOP);               
//                gl.glColor3f(1.0f, 0.0f, 0.0f);   // set the color
                
//                for (Point current_point: gon.points)
//                {
//                    gl.glVertex3f((float)current_point.x_point, (float)current_point.y_point, (float)current_point.z_point);
//                }
//                gl.glEnd();                                                     
//        	}
//        }
        
//        }
    }
 
    // Sets up the OpenGL object.
    public void init(GLAutoDrawable gLDrawable) 
    {
        GL2 gl = gLDrawable.getGL().getGL2();
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        ((Component) gLDrawable).addKeyListener(this);
    }
 
    // Handles reshaping of the display window.
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) 
    {
        GL2 gl = gLDrawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }
        float h = (float) width / (float) height;
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();

        // Copied from the gluPerspective man page:
        // void gluPerspective(GLdouble fovy, GLdouble aspect, GLdouble zNear, GLdouble zFar);
        // Parameters
        // fovy - Specifies the field of view angle, in degrees, in the y direction.
        // aspect - Specifies the aspect ratio that determines the field of view in the x direction.  
        //			The aspect ratio is the ratio of x (width) to y (height).
        // zNear - Specifies the distance from the viewer to the near clipping plane (always positive).
        // zFar - Specifies the distance from the viewer to the far clipping plane (always positive).
        glu.gluPerspective(50.0f, h, 1.0, 1000.0);
        
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    // Clean up when exiting
    public void exit() 
    {
        animator.stop();
        scene_frame.dispose();
        System.exit(0);
    }

    // Needed to implement GLEventListener
    public void dispose(GLAutoDrawable gLDrawable) 
    {
    }
}
