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

import java.lang.Math;
import java.nio.DoubleBuffer;

// This class implements the quaternion used for rotation of the scene
public class Quaternion 
{
	double vertical_axis_x, vertical_axis_y, vertical_axis_z;			// A vector representing the axis of rotation for up/down keys
	double horizontal_axis_x, horizontal_axis_y, horizontal_axis_z;		// A vector representing the axis of rotation for left/right keys
	double rotation_increment = 2 * Math.PI / 360;						// Rotate by one degree per step
	double total_x, total_y, total_z, total_w;							// Result of rotations to this point
	final double camera_distance = -5.0;								// Distance of the camera in the z-axis from the origin
	
	Quaternion()
	{
		// The initial position of the camera is on the positive z-axis, with positive x to the right and positive y up
		vertical_axis_x = 1;
		vertical_axis_y = 0;
		vertical_axis_z = 0;
		
		horizontal_axis_x = 0;
		horizontal_axis_y = 1;
		horizontal_axis_z = 0;
		
		// Initialize to values representing no rotation
		total_x = 0;
		total_y = 0;
		total_z = 0;
		total_w = 1;
	}
	
	// Rotate by rotation_increment in the specified direction and return a 4x4 matrix representing the rotation of the model
	//  from its original position.
	// This function rotates around either the vertical axis or the horizontal axis.
	public DoubleBuffer rotate(String rotation_type)
	{
		double w = 0, x = 0, y = 0, z = 0;

		// Generate quaternion values for the incremental rotation
		if (rotation_type == "up")
		{
			x = vertical_axis_x * Math.sin(-rotation_increment / 2);
			y = vertical_axis_y * Math.sin(-rotation_increment / 2);
			z = vertical_axis_z * Math.sin(-rotation_increment / 2);
			w = Math.cos(-rotation_increment / 2);
		}
		else if (rotation_type == "down")
		{
			x = vertical_axis_x * Math.sin(rotation_increment / 2);
			y = vertical_axis_y * Math.sin(rotation_increment / 2);
			z = vertical_axis_z * Math.sin(rotation_increment / 2);
			w = Math.cos(rotation_increment / 2);
		}
		else if (rotation_type == "right")
		{
			x = horizontal_axis_x * Math.sin(rotation_increment / 2);
			y = horizontal_axis_y * Math.sin(rotation_increment / 2);
			z = horizontal_axis_z * Math.sin(rotation_increment / 2);
			w = Math.cos(rotation_increment / 2);
		}
		else if (rotation_type == "left")
		{
			x = horizontal_axis_x * Math.sin(-rotation_increment / 2);
			y = horizontal_axis_y * Math.sin(-rotation_increment / 2);
			z = horizontal_axis_z * Math.sin(-rotation_increment / 2);
			w = Math.cos(-rotation_increment / 2);
		}
		
		// Use incremental quaternion to update total quaternion
		update_quaternion(w, x, y, z);
						
		return matrix_to_doublebuffer(calculate_rotation_matrix());
	}

	// Conversion to DoubleBuffer and transposition of matrix are necessary so glLoadMatrixd() is happy
	private DoubleBuffer matrix_to_doublebuffer(double[][] input_matrix)
	{
		double[] result_array = new double[16];
		int array_index = 0;
		
		for (int i = 0; i<4; i++)
		{
			for (int j = 0; j<4; j++)
			{
				result_array[array_index] = input_matrix[j][i];
				array_index++;
			}
		}
		
		return DoubleBuffer.wrap(result_array);
	}

	// Update the total quaternion by left multiplying it by the new quaternion.  Remember that multiplication of quaternions 
	//  is not commutative.
	private void update_quaternion(double w, double x, double y, double z)
	{
		double temp_w = total_w;
		double temp_x = total_x;
		double temp_y = total_y;
		double temp_z = total_z;
		
		total_w = w*temp_w - x*temp_x - y*temp_y - z*temp_z;
		total_x = w*temp_x + x*temp_w + y*temp_z - z*temp_y;
		total_y = w*temp_y - x*temp_z + y*temp_w + z*temp_x;
		total_z = w*temp_z + x*temp_y - y*temp_x + z*temp_w;
	}

	// Calculate the rotation matrix representing the current values of the quaternion
	private double[][] calculate_rotation_matrix()
	{
		double[][] result = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
		
//		1-2y2-2z2	2xy-2wz		2xz+2wy		0
//		2xy+2wz		1-2x2-2z2	2yz-2wx		0
//		2xz-2wy		2yz+2wx		1-2x2-2y2	camera_distance
//		0		0		0		1
	
		result[0][0] = 1 - 2*total_y*total_y - 2*total_z*total_z;
		result[0][1] = 2*total_x*total_y - 2*total_w*total_z;
		result[0][2] = 2*total_x*total_z + 2*total_w*total_y;
		result[0][3] = 0;
		result[1][0] = 2*total_x*total_y + 2*total_w*total_z;
		result[1][1] = 1 - 2*total_x*total_x - 2*total_z*total_z;
		result[1][2] = 2*total_y*total_z - 2*total_w*total_x;
		result[1][3] = 0;
		result[2][0] = 2*total_x*total_z - 2*total_w*total_y;
		result[2][1] = 2*total_y*total_z + 2*total_w*total_x;
		result[2][2] = 1 - 2*total_x*total_x - 2*total_y*total_y;
		result[2][3] = camera_distance;			// Translates the camera from the origin
		result[3][0] = 0;
		result[3][1] = 0;
		result[3][2] = 0;
		result[3][3] = 1;
		
		return result;
	}
}