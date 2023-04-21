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

// This class represents a point
public class Point 
{
	double x_point;
	double y_point;
	double z_point;

	// This point is created by this intersection of these three planes
	Plane plane1;
	Plane plane2;
	Plane plane3;
	
	// This point is at the intersection of these three lines, which were also created by the above planes
	Line line1;
	Line line2;
	Line line3;
	
	int name;
	
	public Point(int name, double x, double y, double z, Line l1, Line l2, Line l3, Plane p1, Plane p2, Plane p3) 
	{
		this.name = name;
		x_point = x;
		y_point = y;
		z_point = z;
		line1 = l1;
		line2 = l2;
		line3 = l3;
		plane1 = p1;
		plane2 = p2;
		plane3 = p3;
	}
	
	public double get_x()
	{
		return x_point;
	}
	
	public double get_y()
	{
		return y_point;
	}
	
	public double get_z()
	{
		return z_point;
	}
	
	// This function returns the line shared by this point and the parameter points.  These points *must* share a line and must not be the 
	//  same point or the program will exit with an error; this is only to be called on points adjacent to each other in a polygon.
	public Line get_common_line(Point other_point)
	{
		if (other_point == this)
		{
			System.out.println("ERROR: Point.get_common_line() called on two copies of the same point.");
			System.exit(-1);
		}
		
		if (other_point.line1 == this.line1 || other_point.line2 == this.line1 || other_point.line3 == this.line1)
		{
			return this.line1;
		}
		else if (other_point.line1 == this.line2 || other_point.line2 == this.line2 || other_point.line3 == this.line2)
		{
			return this.line2;
		}
		else if (other_point.line1 == this.line3 || other_point.line2 == this.line3 || other_point.line3 == this.line3)
		{
			return this.line3;
		}
		else
		{
			System.out.println("ERROR: Point.get_common_line() found no common lines.");
			System.exit(-1);
		}
		return null;  // Compiler needs this to feel fulfilled.
	}

	// Get the next line for the current polygon; we require that this point be the vertex of a potential polygon, we have one of the lines
	//  and this will return the other.
	public Line get_next_line(Plane the_plane, Line current_line)
	{
		if (line1 != current_line && (line1.plane1 == the_plane || line1.plane2 == the_plane))
		{
			return line1;
		}
		else if (line2 != current_line && (line2.plane1 == the_plane || line2.plane2 == the_plane))
		{
			return line2;
		}
		else if (line3 != current_line && (line3.plane1 == the_plane || line3.plane2 == the_plane))
		{
			return line3;
		}
		else
		{
			System.out.println("ERROR: Point.get_next_line() unable to find next line.");
			System.exit(-1);
		}
		return null;  // Compiler needs this to feel fulfilled.
	}
}
