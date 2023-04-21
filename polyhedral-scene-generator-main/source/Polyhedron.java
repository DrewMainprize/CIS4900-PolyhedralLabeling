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

// This class represents a Polyhedron
public class Polyhedron 
{
	List<Polygon> polygons = new ArrayList<Polygon>();	// The polygons which make up this polyhedron
	int name;
	
	// Whether this polyhedron has randomly been selected to be visible, based on the poly_probability selected by the user
	boolean is_being_drawn;
	
	// Whether the Conglomerate this polyhedron belongs to has checked it for adjacent polyhedra (which would need to be part
	//  of the same Conglomerate).
	boolean has_been_explored = false;

	public boolean is_being_drawn() 
	{
		return is_being_drawn;
	}

	public void set_is_being_drawn(boolean is_being_drawn) 
	{
		this.is_being_drawn = is_being_drawn;
	}

	// Tells the constituent polygons that they belong to this polyhedron
	public void set_polygon_references()
	{
		for (Polygon i: polygons)
		{
			i.set_polyhedra(this);
		}
	}
	
	// Polygon points must be in counterclockwise order (viewed from outside the polygon) when output to a file.  Thus for each
	//  polygon it's necessary to find a point outside the polyhedron to which it can be compared.
	// This is only an issue for visible polyhedra
	// If a polygon is shared by two visible polyhedra then it will not be drawn or output due to merging, so the arrangement of its points
	//  doesn't matter
	public void make_polygon_points_ccw()
	{
		for (Polygon self: polygons)
		{
			// Choose a point 'a' in this polygon
			// Get the next two consecutive points 'b' and 'c' in self
			Point a = self.get_point(0);
			Point b = self.get_point(1);
			Point c = self.get_point(2);
			
			// Find another polygon in this polyhedron with point 'a'
			Polygon other = null;
			for (Polygon candidate: polygons)
			{
				if (candidate != self && candidate.find_point(a) != -1)
				{
					other = candidate;
					break;
				}
			}			
			
			// Find a point in other which is not in self
			Point otherpoint = null;
			for (Point i: other.get_points())
			{
				if (self.find_point(i) == -1)
				{
					otherpoint = i;
					break;
				}
			}
			
			// Reflect that point in the plane of self to get the viewpoint (ie the point outside the polyhedron) 
			// P0 = (self_x, self_y, self_z)
			double self_x = self.get_plane().get_x_point();
			double self_y = self.get_plane().get_y_point();
			double self_z = self.get_plane().get_z_point();
			// n = (normal_x, normal_y,normal_z)
			double normal_x = self.get_plane().get_normal_x();
			double normal_y = self.get_plane().get_normal_y();
			double normal_z = self.get_plane().get_normal_z();
			// u = P1 - P0
			double u_x = otherpoint.get_x() - self_x;
			double u_y = otherpoint.get_y() - self_y;
			double u_z = otherpoint.get_z() - self_z;
			
			// u1 = (u1_x, u1_y, u1_z) = projection of u on n
			double numerator = (normal_x * u_x) + (normal_y * u_y) + (normal_z * u_z);
			double denominator = (normal_x * normal_x) + (normal_y * normal_y) + (normal_z * normal_z);
			double factor = numerator / denominator;
			double u1_x = factor * normal_x;
			double u1_y = factor * normal_y;
			double u1_z = factor * normal_z;
			
			// u1 is the vector from the closest point in self to otherpoint.  To reflect otherpoint in self, subtract u1 from it twice
			double viewpoint_x = otherpoint.get_x() - 2*u1_x;
			double viewpoint_y = otherpoint.get_y() - 2*u1_y;
			double viewpoint_z = otherpoint.get_z() - 2*u1_z;
			
			// Pass this data to points_are_ccw() to check
			// If the result is false, reverse the points in self
			if (!points_are_ccw(a, b, c, viewpoint_x, viewpoint_y, viewpoint_z))
			{
				self.reverse_points();
			}
		}
	}
	
	// Checks whether points a, b, and c are in counterclockwise order when viewed from point (viewpoint_x, viewpoint_y, viewpoint_z).
	private boolean points_are_ccw(Point a, Point b, Point c, double viewpoint_x, double viewpoint_y, double viewpoint_z)
	{
		// Treat 'a' as the origin
		double bx = b.get_x() - a.get_x();
		double by = b.get_y() - a.get_y();
		double bz = b.get_z() - a.get_z();
		double cx = c.get_x() - a.get_x();
		double cy = c.get_y() - a.get_y();
		double cz = c.get_z() - a.get_z();

		// Use the cross product to find the normal vector
		double normx = by*cz - bz*cy;
		double normy = -(bx*cz - bz*cx);
		double normz = bx*cy - by*cx;
		
		// Vector from viewpoint to 'a'
		double vx = a.get_x() - viewpoint_x;
		double vy = a.get_y() - viewpoint_y;
		double vz = a.get_z() - viewpoint_z;
		
		double dot_product = normx*vx + normy*vy + normz*vz;
		if (dot_product > 0)
		{
			return false;
		}
		return true;
	}

	// Given a plane for which this polyhedron is known to have a polygon, returns the polygon.
	public Polygon find_polygon_in_plane(Plane search_plane)
	{
		for (Polygon i: polygons)
		{
			if (i.plane == search_plane)
			{
				return i;
			}
		}
		System.out.println("ERROR: Polyhedron.find_polygon_in_plane() - no polygon in that plane.");
		System.exit(-1);
		return null;
	}
	
	public void mark_explored()
	{
		this.has_been_explored = true;
	}
	
	public boolean get_explored()
	{
		return this.has_been_explored;
	}

	// Output function for debugging
	public void output_polyhedron()
	{
		System.out.print("\n\nPolyhedron " + name + " has " + polygons.size() + " polygons");
		
		for (Polygon i: polygons)
		{
			i.output_polygon();
		}
	}
}
