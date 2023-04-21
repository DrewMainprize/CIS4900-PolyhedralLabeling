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
import java.util.ArrayList;
import java.util.List;

// This class represents a plane in point-normal form
public class Plane 
{
	double x_point;
	double y_point;
	double z_point;
	
	// theta (elevation in y) is acos of a random number between -1 and 1 (it's 0 at the bottom, PI/2 at the middle, and PI at the top), 
	//  so it's wide at the middle of the sphere and narrow at the top and bottom.
	double theta;
	// phi (rotation in xz) is just a random number on the circle
	double phi;
	
	double normal_x;
	double normal_y;
	double normal_z;
	double plane_constant;  // normal_x*a + normal_y*b + normal_z*c ~= plane_constant if (a, b, c) is in the plane
	int name;
	
	// Lines are created by the intersection of two planes; this is a list of the lines formed by the intersection of this and another plane
	List<Line> lines = new ArrayList<Line>();
	
	public Plane(int name, double x, double y, double z, double theta, double phi) 
	{
		this.name = name;
		x_point = x;
		y_point = y;
		z_point = z;
		this.theta = theta;
		this.phi = phi;
		normal_x = Math.cos(theta - Math.PI/2) * Math.cos(phi);
		normal_y = Math.cos(theta - Math.PI/2) * Math.sin(phi);
		normal_z = Math.sin(theta - Math.PI/2);
		plane_constant = normal_x*x_point + normal_y*y_point + normal_z*z_point;
	}
	
	public double get_x_point()
	{
		return x_point;
	}
	
	public double get_y_point()
	{
		return y_point;
	}
	
	public double get_z_point()
	{
		return z_point;
	}
	
	public double get_normal_x()
	{
		return normal_x;
	}
	
	public double get_normal_y()
	{
		return normal_y;
	}
	
	public double get_normal_z()
	{
		return normal_z;
	}
}
