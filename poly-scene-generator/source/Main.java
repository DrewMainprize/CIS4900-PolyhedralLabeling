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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main 
{
	// Start program and handle command line parameters, if any
	public static void main(String[] args) 
	{		
		int number_of_planes;
		double poly_probability;
		String output_filename = "";

		// Get number of planes
		if (args.length > 0 && isPositiveInteger(args[0]))
		{
			number_of_planes = Integer.parseInt(args[0]);
		}
		else
		{
			number_of_planes = 10;			
		}
		
		// Get probability (as a positive real) of a given polygon being drawn
		if (args.length > 1 && isDouble(args[1]))
		{
			poly_probability = Double.parseDouble(args[1]);

			if (poly_probability < 0 || poly_probability > 1)
			{
				poly_probability = 0.5;
			}
		}
		else
		{
			poly_probability = 0.5;
		}
		
		output_filename = "";
		if (args.length > 2)
		{
			output_filename = args[2];

			int filename_length = output_filename.length();
    		if (filename_length < 5 || !output_filename.endsWith(".obj"))
    		{
    			output_filename = output_filename.concat(".obj");
    		}
		}
		else
		{
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			output_filename = dateFormat.format(date) + ".obj";
		}
		
		// Create Model and View_Controller objects
		Model my_model = new Model();

		// MODIFICATION TO THE ORIGINAL CODE
		// If there are command line args, do not launch the view and instead just call
		// create scene directly with the parameters from the command line
		if(args.length > 0){

			my_model.create_scene(number_of_planes, poly_probability, output_filename);
		
		// Otherwise, continue as normal and launch view to confirm parameters
		} else {
			View my_view = 	new View(number_of_planes, poly_probability, output_filename, my_model);
			my_model.addObserver(my_view);
		}

	}
	
	// Check whether string is a positive integer
    public static boolean isPositiveInteger(String candidate) 
    {
    	if (candidate.isEmpty())
    	{
    		return false;
    	}

    	try 
        {
    		int temp = Integer.parseInt(candidate);
    		
    		if (temp > 0)
    		{
    			return true;
    		}
    		else
    		{
    			return false;
    		}
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
	// Check whether string is a double
    public static boolean isDouble(String candidate) 
    {
    	if (candidate.isEmpty())
    	{
    		return false;
    	}

    	try 
        {
            Double.parseDouble(candidate);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}
