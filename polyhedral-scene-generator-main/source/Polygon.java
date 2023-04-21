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

// This class represents a Polygon
public class Polygon 
{
	ArrayList<Point> points = new ArrayList<Point>();	// Points in this polygon
	Plane plane;
	
	// This polygon has two faces; these booleans indicate whether we've checked if those faces are part of a polyhedron. 
	boolean greaterx_marked = false;
	boolean lesserx_marked = false;
	
	int name;
	Polygon merged_into = null;		// If this polygon has been merged with another, this will point to their child.
	
	// The names of polyhedra using this polygon - set when a polyhedron is successfully created.
	Polyhedron polyhedron_one = null;
	Polyhedron polyhedron_two = null;	
	
	// True once Conglomerate has checked both faces of this polygon for polyhedra
	boolean has_been_explored = false;
	
	// Adds the first two points of this potential polygon.
	public Polygon(Plane the_plane, Point first_point, Point second_point)
	{
		plane = the_plane;
		points.add(first_point);
		points.add(second_point);
	}
	
	// Used when merging two existing polygons to create a new one.
	public Polygon(Plane the_plane, ArrayList<Point> the_points)
	{
		plane = the_plane;
		points = the_points;
	}

	public void set_name(int name)
	{
		this.name = name;
	}
	
	public Plane get_plane()
	{
		return this.plane;
	}
	
	public void add_point(Point the_point)
	{
		points.add(the_point);
	}
	
	// Records the polyhedron that this polygon has been found to be a part of.
	public void set_polyhedra(Polyhedron polyhedron_name)
	{
		if (polyhedron_one == null)
		{
			polyhedron_one = polyhedron_name;
		}
		else if (polyhedron_two == null)
		{
			polyhedron_two = polyhedron_name;
		}
		else
		{
			System.out.println("ERROR in polygon -> set_polyhedra; polygon part of more than two polyhedra.");
			System.exit(-1);
		}

	}
	
	// If this polygon has been merged with another, we need to work with the child instead.
	public Polygon get_current_child()
	{
		if (merged_into != null)
		{
			return merged_into.get_current_child();
		}
		return this;
	}
	
	public void set_child(Polygon child)
	{
		if (merged_into != null)
		{
			merged_into.set_child(child);
		}
		else
		{
			merged_into = child;
		}
	}
	
	public int find_point(Point the_point)
	{
		return points.indexOf(the_point);
	}
	
	public Point get_point(int index)
	{
		return points.get(index);
	}
	
	public int get_num_points()
	{
		return points.size();
	}
	
	public ArrayList<Point> get_points()
	{
		return points;
	}
	
	// current is the index of a point in the 'points' ArrayList, increment is +1 or -1
	// This function gets the index of the next point in the direction of increment, wrapping around as needed
	public int get_next(int current, int increment)
	{
		if (current == 0 && increment == -1)
		{
			return points.size() - 1;
		}
		else if (current == points.size() - 1 && increment == 1)
		{
			return 0;
		}
		else
		{
			return current + increment;
		}
	}

	public void mark_explored()
	{
		this.has_been_explored = true;
	}
	
	public boolean get_explored()
	{
		return this.has_been_explored;
	}
	
	// The points must be listed in counterclockwise order before being output, because the Wavefront .obj format assumes this.
	// If they have been found to be clockwise (which depends on the polyhedron this polygon is in), reverse them.
	public void reverse_points()
	{
		ArrayList<Point> reversed_points = new ArrayList<Point>();
		for (int i = points.size() - 1; i >= 0; i--)
		{
			reversed_points.add(points.get(i));
		}
		points = reversed_points;
	}
	
	// Output function for debugging
	public void output_polygon()
	{
		System.out.println("\nPolygon " + name + " for plane " + plane.name);
		for (Point i: points)
		{
			System.out.println("Point " + i.name + " (" + i.plane1.name + ", " + i.plane2.name + ", " + i.plane3.name 
					+ ") x:" + i.x_point + " y:" + i.y_point + " z:" + i.z_point);
		}
	}

	// Output function for debugging
	public void output_basic_polygon()
	{
		System.out.print("\nPolygon " + name + " for plane " + plane.name + "  ");
		for (Point i: points)
		{
			System.out.print("Point " + i.name + "  ");
		}
		System.out.println("");
	}
}
