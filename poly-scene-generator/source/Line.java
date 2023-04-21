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
import java.util.Collections;

// This class represents a line in point-vector form
public class Line 
{
	double x_point;
	double y_point;
	double z_point;
	double x_vect;
	double y_vect;
	double z_vect;
	
	// This line is formed by the intersection of these two planes
	Plane plane1;
	Plane plane2;
	
	int name;
	
	List<Point> points = new ArrayList<Point>();	// These points are on this line, due to its intersection with a third plane

	// The line is divided into segments by its points.  For each segment and each of the two planes there is a polygon on each side
	//  of the segment, so there are four potential polygons associated with each segment.
	// These boolean arrays indicate whether a given potential polygon has been checked to see if it's part of a valid polygon 
	boolean[] plane1_greaterx_segments; 
	boolean[] plane1_lesserx_segments;
	boolean[] plane2_greaterx_segments; 
	boolean[] plane2_lesserx_segments;
	
	public Line(int name, double x, double y, double z, double x_v, double y_v, double z_v, Plane p1, Plane p2) 
	{
		this.name = name;
		
		x_point = x;
		y_point = y;
		z_point = z;
		
		x_vect = x_v;
		y_vect = y_v;
		z_vect = z_v;

		plane1 = p1;
		plane2 = p2;
	}
	
	public void add_point(Point point)
	{
		points.add(point);
	}
	
	// Once all points have been added to this line, this function initializes the potential polygon arrays and sorts the points
	//  so that adjacent point can be found.
	public void sort_points()
	{
		if (points.size() == 0)
		{
			return;
		}
		
		plane1_greaterx_segments = new boolean[points.size() - 1]; 
		plane1_lesserx_segments = new boolean[points.size() - 1]; 
		plane2_greaterx_segments = new boolean[points.size() - 1]; 
		plane2_lesserx_segments = new boolean[points.size() - 1]; 

		Collections.sort(points, new PointComparator());
	}

	// Returns the index of a point on this line, or -1 if it isn't found
	public int find_index(Point the_point)
	{
		for (int i = 0; i <= points.size() - 1; i++)
		{
			if (points.get(i) == the_point)
			{
				return i;
			}
		}
		return -1;
	}
	
	// Marks the segment between point_index and point_index+1 - doesn't check if this exists
	public void mark_segment(int point_index, Plane plane, String x_type)
	{
		if (plane == plane1)
		{
			if (x_type.equalsIgnoreCase("greater"))
			{
				plane1_greaterx_segments[point_index] = true;
			}
			else
			{
				plane1_lesserx_segments[point_index] = true;
			}
		}
		else
		{
			if (x_type.equalsIgnoreCase("greater"))
			{
				plane2_greaterx_segments[point_index] = true;
			}
			else
			{
				plane2_lesserx_segments[point_index] = true;
			}
		}
	}
}
