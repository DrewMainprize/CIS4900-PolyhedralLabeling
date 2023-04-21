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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;

import poly_package.PolygonEdge;
import poly_package.Conglomerate;
import poly_package.Line;
import poly_package.Plane;
import poly_package.Point;
import poly_package.Polygon;
import poly_package.Polyhedron;

// This class maintains all information about the polygonal scene.
public class Model extends Observable 
{
	// These are the polygons that actually need to be drawn
	HashSet<Polygon> chosen_polygons = new HashSet<Polygon>();
	
	List<Polyhedron> polyhedra = new ArrayList<Polyhedron>();
	List<Polygon> polygons = new ArrayList<Polygon>();
	List<Plane> planes = new ArrayList<Plane>();
	List<Line> lines = new ArrayList<Line>();
	List<Point> points = new ArrayList<Point>();
	List<Conglomerate> conglomerates = new ArrayList<Conglomerate>();
	
	int number_of_planes;
	double poly_probability;
	String output_filename = "";

	Model()
	{
	}
	
	// Create the scene from the initial parameters.
	public void create_scene(int number_of_planes, double poly_probability, String output_filename)
	{
		this.number_of_planes = number_of_planes;
		this.poly_probability = poly_probability;
		this.output_filename = output_filename;
		
		int plane_name = 0;
		int line_name = 0;
		int point_name = 0;
		int polygon_name = 0;
		
		// For each plane
		for (int i = 0; i < number_of_planes; i++)
		{
			// Generate point and normal vector
			double x = 0, y = 0, z = 0;
			
			do
			{
				// Generate a point within the 2x2 cube centered on the origin.
				x = (Math.random() * 2) - 1;
				y = (Math.random() * 2) - 1;
				z = (Math.random() * 2) - 1;
			}
			while (Math.sqrt(x*x + y*y + z*z) > 1);  // Make sure the point is within a sphere of radius 1
			
			// theta (elevation in y) is acos of a random number between -1 and 1 (it's 0 at the bottom, PI/2 at the middle, and PI at the 
			//  top), so it's wide at the middle of the sphere and narrow at the top and bottom.
			// phi (rotation in xz) is just a random number on the circle
			double p = Math.random();
			double theta = Math.acos(1 - 2 * p);
			double phi = Math.random() * 2 * Math.PI;
			
			Plane plane = new Plane(plane_name, x, y, z, theta, phi);
			plane_name++;
			planes.add(plane);
		}
				
		// For each pair of planes, find the line where they intersect
		for (int i = 0; i <= number_of_planes - 2; i++)
		{
			Plane first_plane = planes.get(i);
			for (int j = i + 1; j <= number_of_planes - 1; j++)
			{
				// Use cross-product to find vector component of line of intersection
				Plane second_plane = planes.get(j);
				double cross_x = first_plane.normal_y * second_plane.normal_z - first_plane.normal_z * second_plane.normal_y;
				double cross_y = -(first_plane.normal_x * second_plane.normal_z 
									- first_plane.normal_z * second_plane.normal_x);
				double cross_z = first_plane.normal_x * second_plane.normal_y - first_plane.normal_y * second_plane.normal_x;
				
				// Use equations of planes to find point on line; z is 0 and calculate x and y, which works provided line is 
				//  not parallel to z-axis
				// Uses equation of plane: a(x - x0) + b(y - y0) + c(0 - z0) = 0 -> ax + by = ax0 + by0 + cz0
				// Same thing for second plane, convert first to x= and second to y=, substitute y= for y in x= and simplify
				// This gives us x[1 - (b*a' / b'*a)] = constant, which we then substitute back in to get the y value.
				double a = first_plane.normal_x;
				double b = first_plane.normal_y;
				double c = first_plane.normal_z;
				double a_prime = second_plane.normal_x;
				double b_prime = second_plane.normal_y;
				double c_prime = second_plane.normal_z;
				double x0 = first_plane.x_point;
				double y0 = first_plane.y_point;
				double z0 = first_plane.z_point;
				double x0_prime = second_plane.x_point;
				double y0_prime = second_plane.y_point;
				double z0_prime = second_plane.z_point;
 
				double first_ax_plus_by = a * x0 + b * y0 + c * z0;
				double second_ax_plus_by = a_prime * x0_prime + b_prime * y0_prime + c_prime * z0_prime;
				
				double denominator = 1 - (b * a_prime / (b_prime * a));
				
				double x_point = ((first_ax_plus_by - b * (second_ax_plus_by / b_prime)) / a) / denominator;
				double y_point = (second_ax_plus_by - a_prime * x_point) / b_prime;
				double z_point = 0;
								
				// Find closest point on line to origin; discard line if this is not within sphere
				// If u is the negative of the vector from the origin to (x_point, y_point, z_point), and d is the above 
				//  line, then u + (the projection of u on d) is the intersection of our line and the line through the origin 
				//  perpendicular to d, and is thus the point on our line closest to the origin.
				double u_dot_d = (-x_point) * cross_x + (-y_point) * cross_y + (-z_point) * cross_z;
				double len_d = Math.sqrt(cross_x*cross_x + cross_y*cross_y + cross_z*cross_z);
				double proj_coeff = u_dot_d / (len_d * len_d);
				double orth_x = x_point + proj_coeff * cross_x;
				double orth_y = y_point + proj_coeff * cross_y;
				double orth_z = z_point + proj_coeff * cross_z;
				double len_orth = Math.sqrt(orth_x*orth_x + orth_y*orth_y + orth_z*orth_z);
				
				if (len_orth <= 1)
				{
					// Planes intersect within the sphere, so record the line
					Line line = new Line(line_name, x_point, y_point, z_point, cross_x, cross_y, cross_z, first_plane, second_plane);
					line_name++;
					lines.add(line);

					first_plane.lines.add(line);
					second_plane.lines.add(line);
				}
				else
				{
//					System.out.println("Planes " + first_plane.name + " and " + second_plane.name 
//							+ " are closest to the origin at (" + orth_x + ", " + orth_y + ", " + orth_z + ")");
				}
			}
		}

		// For each line, find the intersection point of the line and each plane it's not part of
		for (int i = 0; i <= lines.size() - 1; i++)
		{
			Line current_line = lines.get(i);

			for (int j = 0; j <= number_of_planes - 1; j++)
			{
				Plane current_plane = planes.get(j);

				if (current_plane != current_line.plane1 && current_plane != current_line.plane2)
				{
					// Go through the list of points that have already been found to see if one is from these three planes; 
					boolean point_exists = false;
					for (Point pt: points)
					{
						if ((pt.plane1 == current_plane || pt.plane1 == current_line.plane1 
								|| pt.plane1 == current_line.plane2)
							&& (pt.plane2 == current_plane || pt.plane2 == current_line.plane1 
									|| pt.plane2 == current_line.plane2)
							&& (pt.plane3 == current_plane || pt.plane3 == current_line.plane1 
									|| pt.plane3 == current_line.plane2))
						{
							point_exists = true;
						}
							
					}
					if (point_exists)
					{
						continue;
					}
					
					// If we get here we've found a new point; calculate where the line and plane intersect.
					// The line is (x0, y0, z0) + t(xv, yv, zv), so all we need to do is determine t
					double x0 = current_plane.x_point;
					double y0 = current_plane.y_point;
					double z0 = current_plane.z_point;
					double a = current_plane.normal_x;
					double b = current_plane.normal_y;
					double c = current_plane.normal_z;
					double xp = current_line.x_point;
					double yp = current_line.y_point;
					double zp = current_line.z_point;
					double xv = current_line.x_vect;
					double yv = current_line.y_vect;
					double zv = current_line.z_vect;
					// Start with the scalar equation of the plane, substitute in the parametric equation of the line for the 
					//  point where the line intersects the plane, and solve for t.
					double t = (a * (x0 - xp) + b * (y0 - yp) + c * (z0 - zp)) / (a * xv + b * yv + c * zv);
					
					double x_value = xp + t * xv;
					double y_value = yp + t * yv;
					double z_value = zp + t * zv;
					
					// If the point is outside the sphere, discard it
					if (Math.sqrt(x_value*x_value + y_value*y_value + z_value*z_value) > 1)
					{
						continue;
					}
					
					// This point is at the intersection of the three lines defined by these planes.
					// We already know one line, and can find the other two by looking for lines which belong to the other two planes.
					Line second_line = null, third_line = null;
					for (int k = i + 1; k <= lines.size() - 1; k++)
					{
						Line temp_line = lines.get(k);

						if ((temp_line.plane1 == current_plane && temp_line.plane2 == current_line.plane1)
								|| temp_line.plane1 == current_plane && temp_line.plane2 == current_line.plane2
								|| temp_line.plane2 == current_plane && temp_line.plane1 == current_line.plane1
								|| temp_line.plane2 == current_plane && temp_line.plane1 == current_line.plane2)
						{
							if (second_line == null)
							{
								second_line = temp_line;
							}
							else
							{
								third_line = temp_line;
								break;
							}
						}
					}
					
					if (second_line == null || third_line == null)
					{
						System.out.println("ERROR: Matching lines not found");
						System.exit(-1);
					}

					Point new_point = new Point(point_name, x_value, y_value, z_value, current_line, second_line, third_line,
													current_plane, current_line.plane1, current_line.plane2);
					point_name++;

					points.add(new_point);
					// Add point to the relevant lines
					current_line.add_point(new_point);
					second_line.add_point(new_point);
					third_line.add_point(new_point);
				}
			}
		}
		
		// For each line, sort the points
		for (int i = 0; i <= lines.size() - 1; i++)
		{
			lines.get(i).sort_points();
		}
		
//		display_all();
		
		// Find all polygons:
		// Iterate through each line and check whether each of the four components of each segment are marked as used
		// If not, generate that polygon and mark the segments
		for (int i = 0; i <= lines.size() - 1; i++)
		{
			Line current_line = lines.get(i);
			
			// If there are not at least two points on this line, it has no valid segments
			if (current_line.points.size() <= 1)
			{
				continue;
			}
			
			// Iterate through line segments (delineated by points on this line).
			for (int j = 0; j <= current_line.plane1_greaterx_segments.length - 1; j++)
			{
				if (current_line.plane1_greaterx_segments[j] == false)
				{
					// Call function to attempt to create polygon
					create_polygon(current_line.points.get(j), current_line.points.get(j + 1), current_line, j, 
									current_line.plane1, "greater", polygon_name);
					
					if (polygons.size() == polygon_name + 1)
					{
						polygon_name++;
					}
				}

				if (current_line.plane1_lesserx_segments[j] == false)
				{
					// Call function to attempt to create polygon
					create_polygon(current_line.points.get(j), current_line.points.get(j + 1), current_line, j, 
							current_line.plane1, "lesser", polygon_name);

					if (polygons.size() == polygon_name + 1)
					{
						polygon_name++;
					}
				}

				if (current_line.plane2_greaterx_segments[j] == false)
				{
					// Call function to attempt to create polygon
					create_polygon(current_line.points.get(j), current_line.points.get(j + 1), current_line, j, 
							current_line.plane2, "greater", polygon_name);
					
					if (polygons.size() == polygon_name + 1)
					{
						polygon_name++;
					}
				}

				if (current_line.plane2_lesserx_segments[j] == false)
				{
					// Call function to attempt to create polygon
					create_polygon(current_line.points.get(j), current_line.points.get(j + 1), current_line, j, 
							current_line.plane2, "lesser", polygon_name);

					if (polygons.size() == polygon_name + 1)
					{
						polygon_name++;
					}
				}
			}
		}
		
//		for (Polygon i: polygons)
//		{
//			i.output_polygon();
//		}
		
		// Uses the polygons to create the polyhedra
		create_polyhedra();

		List<Polyhedron> chosen_polyhedra = new ArrayList<Polyhedron>();	// The polyhedra which will actually be drawn

		for (Polyhedron i: polyhedra)
		{
			// Determine whether the polygons for this polyhedron will actually be drawn
			if (Math.random() < poly_probability)
			{
				i.set_is_being_drawn(true);
				i.make_polygon_points_ccw();
				chosen_polygons.addAll(i.polygons);
				chosen_polyhedra.add(i);
			}
		}
		
		// If two polyhedra share a polygon and are both being drawn, that polygon should not be drawn.  To prevent this, the polyhedra must 
		//  be merged by merging the polygons which share an edge with the polygon shared by the polyhedra.  This will create a child 
		//  polyhedron.  It may be necessary to merge child polyhedra, which adds additional complications.
		int conglomerate_name = 0;
		
		// A conglomerate is a contiguous mass of visible polyhedra.  The following loop iterates through all chosen polyhedra and assigns
		//  each to a conglomerate.
		for (Polyhedron current_poly: chosen_polyhedra)
		{
			if (current_poly.get_explored())
			{
				continue;
			}
			
			Conglomerate new_conglomerate = new Conglomerate(conglomerate_name, current_poly);
			conglomerate_name++;
			current_poly.mark_explored();
			new_conglomerate.explore(chosen_polyhedra);
			polygon_name = new_conglomerate.merge(polygon_name);
			conglomerates.add(new_conglomerate);
		}
				
		output_to_file(output_filename);
		
		// Notify the view that the results are available.
		setChanged();
		notifyObservers(conglomerates);
	}
	
	// Creates polyhedra from the polygons
	void create_polyhedra()
	{
		int polyhedron_name = 0;

		// Iterate through all polygons
		for (Polygon p: polygons)
		{
			// Check each side - i=0 means greaterx, i=1 means lesserx
			for (int i = 0; i <= 1; i++)
			{
				// If the current side has already been marked, continue
				if ((i == 0 && p.greaterx_marked == true) || (i == 1 && p.lesserx_marked == true))
				{
					continue;
				}
				
				// Otherwise, mark and start polyhedron creation by pushing its edges onto a list
				boolean side_is_greaterx = false;
				if (i == 0)
				{
					p.greaterx_marked = true;
					side_is_greaterx = true;
				}
				if (i == 1)
				{
					p.lesserx_marked = true;
					side_is_greaterx = false;
				}
				
				Polyhedron current_polyhedron = new Polyhedron();
				current_polyhedron.polygons.add(p);
				
				// List of currently unmatched edges for the current polyhedron.  Each edge is connected to two polygons, and 
				//  all must be matched or the polyhedron fails.
				List<PolygonEdge> edges = new ArrayList<PolygonEdge>();

				// Create edges for first polygon
				update_polygon_edges(edges, p, true, side_is_greaterx);
				
				boolean polyhedron_failed = false;
				
				// While 'edges' is not empty, get an edge and find the matching polygon.  It will share the edge's points, will 
				//  not share its plane, and the points of this new polygon will be on the proper x-side of the edge's 
				//  polygon.  For this last part we need a function which projects a third point from the new polygon onto the 
				//  plane of the old polygon, and returns whether the x is greater or less than the projected point. 
				while (edges.size() > 0)
				{
					PolygonEdge current_edge = edges.get(0);
					Plane matching_plane;
					Polygon matching_polygon = null;
					
					// current_edge is part of a line which is composed of two planes.  The plane we're looking for is the 
					//  one which is not recorded in current_edge
					if (current_edge.the_plane != current_edge.the_line.plane1)
					{
						matching_plane = current_edge.the_line.plane1;
					}
					else
					{
						matching_plane = current_edge.the_line.plane2;
					}
					
					// Find this edge's other polygon
					for (Polygon current_polygon:polygons)
					{
						boolean matched_point_one = false;
						boolean matched_point_two = false;
						
						// There can only be two polygons which are in the correct plane and include both points.
						if (current_polygon.plane == matching_plane)
						{
							// Iterate through the polygon's points, looking for both of current_edge's points
							for (Point current_point:current_polygon.points)
							{
								if (current_point == current_edge.first_point)
								{
									matched_point_one = true;
								}
								else if (current_point == current_edge.second_point)
								{
									matched_point_two = true;
								}
							}
							
							if (matched_point_one == true && matched_point_two == true)
							{
								// Get a third point from the candidate polygon
								Point third_point = null;
								
								for (Point pt:current_polygon.points)
								{
									if (pt != current_edge.first_point && pt != current_edge.second_point)
									{
										third_point = pt;
										break;
									}
								}
								
								// Check if this polygon is on the correct side of the polygon that created current_edge
								if (current_edge.plane_side_is_greaterx 
											&& is_greaterx_planar(current_edge.the_plane, third_point)
									|| !current_edge.plane_side_is_greaterx 
											&& !is_greaterx_planar(current_edge.the_plane, third_point))
								{
									matching_polygon = current_polygon;
									break;
								}
							}
						}
					}
					
					if (matching_polygon != null)
					{
						// Add the new polygon to the polyhedron.  
						current_polyhedron.polygons.add(matching_polygon);
						
						// Determine whether we're using the greater or lesser x side and mark it.  
						// Get a third point from the previous polygon
						Point third_point = null;
						
						for (Point pt:current_edge.creator_polygon.points)
						{
							if (pt != current_edge.first_point && pt != current_edge.second_point)
							{
								third_point = pt;
								break;
							}
						}

						boolean new_polygon_is_greaterx;
						
						if (is_greaterx_planar(matching_polygon.plane, third_point))
						{
							matching_polygon.greaterx_marked = true;
							new_polygon_is_greaterx = true;
						}
						else
						{
							matching_polygon.lesserx_marked = true;
							new_polygon_is_greaterx = false;
						}

						// Update edges for new polygon
						update_polygon_edges(edges, matching_polygon, false, new_polygon_is_greaterx);
					}
					else
					{
						polyhedron_failed = true;
						
						// Remove the edge we were looking at; if there were a match this would be done in update_polygon_edges 
						edges.remove(0);
					}
				} // END while (edges.size() > 0)
				
				if(!polyhedron_failed)
				{
					// Add the new polyhedron to the list
					current_polyhedron.name = polyhedron_name;
					polyhedron_name++;
					polyhedra.add(current_polyhedron);
					current_polyhedron.set_polygon_references();
				}
			}
		}
	}
	
	// Takes a point and a plane, and returns true if the point is on the greaterx side of the plane (ie: if the x-value of
	//  the point is greater than the x-value of the point's projection on the plane), false otherwise
	boolean is_greaterx_planar(Plane the_plane, Point the_point)
	{
		// There's a line between the_point and the projection of the_point on the plane; the vector part of this line is just the normal.
		// The projected point uniquely satisfies the equations of both the line and the plane, so combine the two and solve for t from the
		//   vector equation of the line.
		// t = (plane_constant - dot(normal, the_point)) / ||normal||^2
		double dot_product = the_plane.normal_x * the_point.x_point 
								+ the_plane.normal_y * the_point.y_point 
								+ the_plane.normal_z * the_point.z_point;
		double norm = Math.sqrt(the_plane.normal_x * the_plane.normal_x
								+ the_plane.normal_y * the_plane.normal_y
								+ the_plane.normal_z * the_plane.normal_z);
		double t = (the_plane.plane_constant - dot_product) / (norm * norm);
		
		double projected_x = the_point.x_point + t * the_plane.normal_x;
		
		if (projected_x < the_point.x_point)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// 'edges' is a list of currently unmatched edges for the current polyhedron.  Each edge is connected to two polygons, and 
	//  all must be matched or the polyhedron fails.
	// Iterate through edges of current polygon to update edges; remove matched edges and add new ones
	void update_polygon_edges(List<PolygonEdge> edges, Polygon p, boolean is_initial_polygon, boolean side_is_greaterx)
	{
		// Remember first point so we can connect it to last point
		Point first_point = p.points.get(0);
		Point previous_point = first_point;
		
		// Iterate through points to create line segments (edges)
		for (int j = 1; j <= p.points.size(); j++)
		{
			PolygonEdge new_edge = new PolygonEdge();
			new_edge.first_point = previous_point;
			
			// If we're looking at the last point in the list, connect it with the starting point
			if (j < p.points.size())
			{
				new_edge.second_point = p.points.get(j);
			}
			else
			{
				new_edge.second_point = first_point;
			}

			new_edge.the_plane = p.plane;
			new_edge.creator_polygon = p;
			new_edge.plane_side_is_greaterx = side_is_greaterx;
			
			// Figure out which line the points share
			if (new_edge.first_point.line1 == new_edge.second_point.line1)
				new_edge.the_line = new_edge.first_point.line1;
			else if (new_edge.first_point.line1 == new_edge.second_point.line2)
				new_edge.the_line = new_edge.first_point.line1;
			else if (new_edge.first_point.line1 == new_edge.second_point.line3)
				new_edge.the_line = new_edge.first_point.line1;
			else if (new_edge.first_point.line2 == new_edge.second_point.line1)
				new_edge.the_line = new_edge.first_point.line2;
			else if (new_edge.first_point.line2 == new_edge.second_point.line2)
				new_edge.the_line = new_edge.first_point.line2;
			else if (new_edge.first_point.line2 == new_edge.second_point.line3)
				new_edge.the_line = new_edge.first_point.line2;
			else if (new_edge.first_point.line3 == new_edge.second_point.line1)
				new_edge.the_line = new_edge.first_point.line3;
			else if (new_edge.first_point.line3 == new_edge.second_point.line2)
				new_edge.the_line = new_edge.first_point.line3;
			else if (new_edge.first_point.line3 == new_edge.second_point.line3)
				new_edge.the_line = new_edge.first_point.line3;
			else
			{
				System.out.println("ERROR: points in polygon share no lines");
				System.exit(-1);
			}
			
			// If this isn't the initial polygon, check if this edge matches an existing one.  If so, remove it from the list of 
			//  unmatched edges.  If not, add it.
			if (is_initial_polygon)
			{
				edges.add(new_edge);
			}
			else
			{
				boolean found_edge = false;
				
				for (int e = 0; e < edges.size(); e++)
				{
					PolygonEdge current = edges.get(e);
					
					// Check whether points match, meaning 'current' is the same as the edge we're considering
					if ((current.first_point == new_edge.first_point && current.second_point == new_edge.second_point)
							|| current.first_point == new_edge.second_point && current.second_point == new_edge.first_point)
					{
						edges.remove(e);
						found_edge = true;
						break;
					}
				}

				if (!found_edge)
				{
					edges.add(new_edge);
				}
			}
			
			previous_point = new_edge.second_point;
		}
	}

	// This function attempts to create a polygon starting with an unexplored line segment (edge)
	void create_polygon(Point first_point, Point second_point, Line current_line, int segment_j, Plane the_plane, 
								String x_type, int current_name)
	{
		// Create polygon object with plane and points - this will be discarded if the attempt to create a polygon fails.
		Polygon polygon = new Polygon(the_plane, first_point, second_point);
		
		// Mark first segment
		current_line.mark_segment(segment_j, the_plane, x_type);

		boolean finished = false;
		Point original_point = first_point;
		Point previous_point = first_point;
		Point current_point = second_point;
		Point candidate = null;
		Line next_line;
		
		do
		{
			int index_of_segment = -1;			// The index (within its Line object) of the new line segment (edge) for our polygon.
			boolean next_point_found = false;
			
			// Find line associated with second point
			next_line = current_point.get_next_line(the_plane, current_line);
			
			// Find point, segment, and side (greater_x/lesser_x) for this line
			// The next point for the new line (if it exists) is on the same side of the old line as the polygon
			int point_index = next_line.find_index(current_point);
						
			if (point_index == -1)
			{
				System.out.println("ERROR: Point not found in create_polygon()");
				System.exit(-1);
			}

			// If there's a (point_index - 1) point, project it on the current line to determine whether it's x_greater
			if (point_index > 0)
			{
				// Determine whether index - 1 is our next point
				candidate = next_line.points.get(point_index - 1);
				index_of_segment = point_index - 1;
				
				// Project the point on the current line, using previous_point as the origin
				double u_x = candidate.x_point - previous_point.x_point;
				double u_y = candidate.y_point - previous_point.y_point;
				double u_z = candidate.z_point - previous_point.z_point;
				
				double dot_ud = (u_x * current_line.x_vect) + (u_y * current_line.y_vect) + (u_z * current_line.z_vect);
				double len_u = Math.sqrt(current_line.x_vect * current_line.x_vect + current_line.y_vect * current_line.y_vect 
											+ current_line.z_vect * current_line.z_vect);
				double proj_x = dot_ud * current_line.x_vect / (len_u * len_u);
				
				// Move the result back to where it was relative to previous_point.
				proj_x += previous_point.x_point;

				// If this point is the greater_x we were looking for.
				if (x_type.equalsIgnoreCase("greater") && candidate.x_point > proj_x)
				{
					// Add this point to the polygon unless it's the point we started the polygon with, in which case it's already there. 
					if (original_point != candidate)
					{
						polygon.add_point(candidate);
					}
						
					current_line = next_line;
					next_point_found = true;
				}
				// If this point is the lesser_x we were looking for.
				else if (x_type.equalsIgnoreCase("lesser") && candidate.x_point < proj_x)
				{
					// Add this point to the polygon unless it's the point we started the polygon with, in which case it's already there. 
					if (original_point != candidate)
					{
						polygon.add_point(candidate);
					}
						
					current_line = next_line;
					next_point_found = true;
				}
			}
			// If point_index - 1 didn't succeed, try point_index + 1
			if (next_point_found == false && point_index < next_line.points.size() - 1)
			{
				// Determine whether index + 1 is our next point
				candidate = next_line.points.get(point_index + 1);
				index_of_segment = point_index;
				
				// Project the point on the current line, using previous_point as the origin
				double u_x = candidate.x_point - previous_point.x_point;
				double u_y = candidate.y_point - previous_point.y_point;
				double u_z = candidate.z_point - previous_point.z_point;
				
				double dot_ud = (u_x * current_line.x_vect) + (u_y * current_line.y_vect) + (u_z * current_line.z_vect);
				double len_u = Math.sqrt(current_line.x_vect * current_line.x_vect + current_line.y_vect * current_line.y_vect 
											+ current_line.z_vect * current_line.z_vect);
				double proj_x = dot_ud * current_line.x_vect / (len_u * len_u);

				// Move the result back to where it was relative to previous_point.
				proj_x += previous_point.x_point;
				
				// If this point is the greater_x we were looking for.
				if (x_type.equalsIgnoreCase("greater") && candidate.x_point > proj_x)
				{
					// Add this point to the polygon unless it's the point we started the polygon with, in which case it's already there. 
					if (original_point != candidate)
					{
						polygon.add_point(candidate);
					}
						
					current_line = next_line;
					next_point_found = true;
				}
				// If this point is the lesser_x we were looking for.
				else if (x_type.equalsIgnoreCase("lesser") && candidate.x_point < proj_x)
				{
					// Add this point to the polygon unless it's the point we started the polygon with, in which case it's already there. 
					if (original_point != candidate)
					{
						polygon.add_point(candidate);
					}
						
					current_line = next_line;
					next_point_found = true;
				}
			}
			if (next_point_found == false)
			{
				// If we get here there is no next point; abandon polygon.
				return;
			}
			
			// The polygon is on the same side of the line of the new edge as previous_point; we need to know whether this is greaterx or 
			//  lesserx.  To find out, project previous_point onto the line.  If previous_point.x > projetion.x, it's greater.
			
			// Make current_point the origin
			double u_x = previous_point.x_point - candidate.x_point;
			double u_y = previous_point.y_point - candidate.y_point;
			double u_z = previous_point.z_point - candidate.z_point;
			
			// Project the point on the current line, using previous_point as the origin
			double dot_ud = (u_x * next_line.x_vect) + (u_y * next_line.y_vect) + (u_z * next_line.z_vect);
			double len_u = Math.sqrt(next_line.x_vect * next_line.x_vect + next_line.y_vect * next_line.y_vect 
										+ next_line.z_vect * next_line.z_vect);
			double proj_x = dot_ud * next_line.x_vect / (len_u * len_u);
			
			// Move the result back to where it was relative to current_point.
			proj_x += candidate.x_point;

			// This is the x-value of the vector from the previous point perpendicular to the line
			// We don't know which side of the segment to mark until we've determined the new x_type

			// Polygon is on greaterx side of new segment
			if (proj_x < previous_point.x_point)
			{
				x_type = "greater";
			}
			// Polygon is on lesserx side of new segment
			else
			{
				x_type = "lesser";
			}
			// Mark the segment appropriately so we don't look for a polygon here again.
			next_line.mark_segment(index_of_segment, the_plane, x_type);

			// If we've looped back around to our starting point, the polygon is complete.
			if (original_point == candidate)
			{
				polygon.set_name(current_name);
//				polygon.output_polygon();
				polygons.add(polygon);
				return;
			}

			previous_point = current_point;
			current_point = candidate;
		} while (finished == false);
	}

	// Displays all planes, lines, and points for debugging
	void display_all()
	{
		for (Plane i:planes)
		{
			System.out.print("Plane " + i.name + " contains lines ");
			for (Line j:i.lines)
			{
				System.out.print(j.name + ", ");
			}
			System.out.println();
		}
		System.out.println();

		for (Line j:lines)
		{
			System.out.print("Line " + j.name + " contains points ");
			for (Point k:j.points)
			{
				System.out.print(k.name + ", ");
			}
			System.out.println();
		}
		
		for (Point k:points)
		{
			System.out.println("Point " + k.name + " (" + k.x_point + ", " + k.y_point + ", " + k.z_point + ") Planes (" 
					+ k.plane1.name + ", " + k.plane2.name + ", " + k.plane3.name + ") Lines (" + k.line1.name 
					+ ", " + k.line2.name + ", " + k.line3.name + ")");
		}

	}
	
	// This function outputs the polygons being drawn to a Wavefront .obj file.
	void output_to_file(String output_filename)
	{
		PrintWriter writer = null;
		try 
		{
			// Open the file
			writer = new PrintWriter(output_filename, "UTF-8");
			writer.println("# Polygons representing a polyhedral scene");
			writer.println("");

			// Iterate through conglomerates, and within each iterate through outline_draw_list to get polygons to draw
			for (Conglomerate conglo: conglomerates)
			{
				List<Polygon> drawables = conglo.get_outline_draw_list();

				// Iterate through chosen polygons and draw each
				for (Polygon gon: drawables)
				{
					int point_count = 0;
					for (Point current_point: gon.points)
					{
						point_count--;
						// Output points
						writer.println("v " + current_point.x_point + " " + current_point.y_point + " " + current_point.z_point);
					}
					
					// Output face data based on point_count
					writer.print("f ");
					for (int i = point_count; i < 0; i++)
					{
						writer.print(i + " ");
					}
					writer.println();
					writer.println();
				}
			}
		} 
		catch (IOException ex) 
		{
			System.out.println("Error writing to file: " + ex);
		} 
		finally 
		{
			try {writer.close();} catch (Exception ex) {}
		}
	}
}
