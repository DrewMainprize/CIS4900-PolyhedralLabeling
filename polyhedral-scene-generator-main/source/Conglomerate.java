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

// This class represents a contiguous mass of visible polyhedra, and merges shared edges to prevent extraneous lines from being drawn.
// This class starts with an initial polyhedron (from among those selected to be visible), collects all visible polyhedra which share a 
//  polygon with that polyhedron, and all visible polyhedra which share a polygon with any of those polyhedra, etc.
// If both faces of a polygon are part of a visible polyhedron, then that polygon should not be drawn, and other polygons in the two 
//  polyhedra will need to be merged.
public class Conglomerate 
{
	List<Polyhedron> collected_polyhedra = new ArrayList<Polyhedron>();	// Polyhedra in this conglomerate
	List<Polygon> explore_list = new ArrayList<Polygon>();				// Polygons being checked for redundancy
	List<Polygon> merge_list = new ArrayList<Polygon>();				// Redundant polygons which will not be drawn
	List<Polygon> outline_draw_list = new ArrayList<Polygon>();			// Polygon outlines to actually be drawn
	List<Polygon> draw_list = new ArrayList<Polygon>();			// Polygons to actually be drawn - can't be concave so can't just use above

	int name;
	
	public Conglomerate(int conglomerate_name, Polyhedron initial_polyhedron)
	{
		name = conglomerate_name;
		collected_polyhedra.add(initial_polyhedron);
		explore_list = initial_polyhedron.polygons;
	}
	
	// polyhedra is the list of polyhedra which are to be drawn and have not yet been placed in a conglomerate
	// explore() determines which of these belong in this conglomerate and adds them
	public void explore(List<Polyhedron> polyhedra)
	{
		Polygon current_polygon;
		
		// Iterate over the polygons in explore_list.  Can't use for-each here because additional polygons are being added.
		for (int i = 0; i < explore_list.size(); i++)
		{
			current_polygon = explore_list.get(i);
			// Ignore polygons that have been dealt with
			if (current_polygon.get_explored())
			{
				continue;
			}
			
			// If this polygon is shared by two drawn polyhedra, then both polyhedra need to be in this Conglomerate
			// It should be the case that one already is (since current_polygon was added to the explore list) and one isn't (since 
			//  current_polygon has not been marked as explored).
			if (current_polygon.polyhedron_two != null)  // If this polygon is used by two polyhedra
			{
				// If the first polyhedron is a visible polyhedron and is not already part of this conglomerate, add it
				if (polyhedra.indexOf(current_polygon.polyhedron_one) != -1 
						&& collected_polyhedra.indexOf(current_polygon.polyhedron_one) == -1)
				{
					collected_polyhedra.add(current_polygon.polyhedron_one);
					current_polygon.polyhedron_one.mark_explored();
						
					// Check the faces of this polyhedron against the explore list.  Add the ones that aren't already there.
					// If a face is already there, mark it and add it to the merge list.
					for (Polygon new_face: current_polygon.polyhedron_one.polygons)
					{
						if (explore_list.indexOf(new_face) == -1)
						{
							explore_list.add(new_face);
						}
						else
						{
							new_face.mark_explored();
							merge_list.add(new_face);
						}
					}
				}
				// Otherwise if the second polyhedron is a chosen polyhedron and is not already part of this conglomerate, add it
				else if (polyhedra.indexOf(current_polygon.polyhedron_two) != -1 
						&& collected_polyhedra.indexOf(current_polygon.polyhedron_two) == -1)
				{
					collected_polyhedra.add(current_polygon.polyhedron_two);
					current_polygon.polyhedron_two.mark_explored();
						
					// Check the faces of this polyhedron against the explore list.  Add the ones that aren't already there.
					// If a face is already there, mark it and add it to the merge list.
					for (Polygon new_face: current_polygon.polyhedron_two.polygons)
					{
						if (explore_list.indexOf(new_face) == -1)
						{
							explore_list.add(new_face);
						}
						else
						{
							new_face.mark_explored();
							merge_list.add(new_face);
						}
					}
				}
				else // Two polyhedra exist, but only one is drawn; transfer this polygon to the draw lists
				{
					outline_draw_list.add(current_polygon);
					draw_list.add(current_polygon);
					current_polygon.mark_explored();
				}				
			}
			else // Only one polyhedron exists; transfer this polygon to the draw lists
			{
				outline_draw_list.add(current_polygon);
				draw_list.add(current_polygon);
				current_polygon.mark_explored();
			}
		}
	}

	// If a polygon is in merge_list, this means that both of its polyhedra are being displayed.
	// Thus the polygon itself should not be drawn, and each of its edges are shared by two other polygons which must be combined into one.
	// merge() returns the name of the next Polygon that will be created.
	public int merge(int polygon_name)
	{
		for (Polygon merging_polygon: merge_list)
		{
			Point current_point, next_point;
			
			// For each edge of the redundant polygon, check whether we need to combine polygons which share that edge
			for (int i = 0; i < merging_polygon.points.size(); i++)
			{
				current_point = merging_polygon.points.get(i);
				if (i == merging_polygon.points.size() - 1)
				{
					next_point = merging_polygon.points.get(0);
				}
				else
				{
					next_point = merging_polygon.points.get(i + 1);
				}

				// Find the line common to current and next point
				Line common_line = current_point.get_common_line(next_point);
				
				// Find the plane of the line which doesn't match that of the current polygon  
				Plane common_plane = null;
				if (common_line.plane1 != merging_polygon.plane)
				{
					common_plane = common_line.plane1;
				}
				else if (common_line.plane2 != merging_polygon.plane)
				{
					common_plane = common_line.plane2;
				}
				else
				{
					System.out.println("ERROR: Conglomerate.merge() both planes of line of merging polygon are the same.");
					System.exit(-1);
				}
				
				// For each of the merging polyhedra, find the polygon in that plane, and trace it down to its current children 
				Polygon first_merger, second_merger;
				first_merger = merging_polygon.polyhedron_one.find_polygon_in_plane(common_plane);
				second_merger = merging_polygon.polyhedron_two.find_polygon_in_plane(common_plane);
				
				// Don't combine if one of the polygons is in the merge list
				if (merge_list.contains(first_merger) || merge_list.contains(second_merger))
				{
					continue;
				}
				
				// The basic polygons may already have combined with other polygons; get the current final children
				first_merger = first_merger.get_current_child();
				second_merger = second_merger.get_current_child();

				// In complex cases the polygons may already have been combined, in which case continue.
				if (first_merger == second_merger)
				{
					continue;
				}

				// We need to combine the polygons
				ArrayList<Point> new_points = new ArrayList<Point>();
				new_points = combine_polygons(first_merger, second_merger, current_point, next_point);
				Polygon new_polygon = new Polygon(common_plane, new_points);
				new_polygon.set_name(polygon_name);
				polygon_name++;
				
				// Remove the parents from the outline_draw_list and add the child - we can do this because it's okay if the outlines 
				//  are concave.
				outline_draw_list.remove(first_merger);
				outline_draw_list.remove(second_merger);
				outline_draw_list.add(new_polygon);
				
				first_merger.set_child(new_polygon);
				second_merger.set_child(new_polygon);
			}
		}
		
		return polygon_name;
	}
	
	// This function combines the two lists of points into a new list with no duplicates.
	// The duplicated points are assumed to be in a sequence, and only the endpoints of this sequence will be included in the new list.
	// current_point and next_point are points which exist and are adjacent to each other in both lists.
	private ArrayList<Point> combine_polygons(Polygon first_poly, Polygon second_poly, Point current_point, Point next_point)
	{
		// Find the index of current_point and next_point in each polygon
		int first_current = first_poly.find_point(current_point);
		int second_current = second_poly.find_point(current_point);
		int first_next = first_poly.find_point(next_point);
		int second_next = second_poly.find_point(next_point);
		
		// When output to the Wavefront .obj file, the points in the polygons must be in counterclockwise order (when viewed from outside
		//  the polyhedron).  This was done when each polygon was added to its polyhedron, but it must be maintained when the polygons 
		//  are merged.  To ensure this, we need to ensure that travel from first_current directly to first_next is clockwise within 
		//  first_poly, since when creating the merged polygon we will be traveling from first_current to first_next the other way. 
		if (first_current > first_next || (first_current == 0 && first_next != 1))
		{
			Point temp_point = current_point;
			current_point = next_point;
			next_point = temp_point;
			first_current = first_poly.find_point(current_point);
			second_current = second_poly.find_point(current_point);
			first_next = first_poly.find_point(next_point);
			second_next = second_poly.find_point(next_point);
		}
		
		// Determine directions in each case
		// We need to move starting from current and away from next in order to find more shared points, so we need to travel the 
		//  opposite of the direction to next
		boolean first_forward = false;	// false here means that in the first list we decrease the index to get from current to next
		boolean second_forward = false;
		int first_increment_direction = 1;
		int second_increment_direction = 1;

		if (first_current == first_next - 1 || (first_next == 0 && first_current != 1))
		{
			first_forward = true;
			first_increment_direction = -1;
		}
		if (second_current == second_next - 1 || (second_next == 0 && second_current != 1))
		{
			second_forward = true;
			second_increment_direction = -1;
		}

		// Search for matches 'before' first_current; first_sequence_start and second_sequence_start record the point before the actual 
		//  first match
		int first_sequence_start = first_current;
		int second_sequence_start = second_current;
		
		do
		{
			first_sequence_start = first_poly.get_next(first_sequence_start, first_increment_direction);
			second_sequence_start = second_poly.get_next(second_sequence_start, second_increment_direction);
		} while (first_poly.get_point(first_sequence_start) == second_poly.get_point(second_sequence_start));

		// initial_shared_point is the initial point in the shared sequence
		int initial_shared_index = first_poly.get_next(first_sequence_start, -first_increment_direction);
		Point initial_shared_point = first_poly.get_point(initial_shared_index);
		
		// Search for matches 'after' first_next; first_sequence_end and second_sequence_end record the point after the actual last match
		int first_sequence_end = first_next;
		int second_sequence_end = second_next;
		
		do
		{
			first_sequence_end = first_poly.get_next(first_sequence_end, -first_increment_direction);
			second_sequence_end = second_poly.get_next(second_sequence_end, -second_increment_direction);
		} while (first_poly.get_point(first_sequence_end) == second_poly.get_point(second_sequence_end));

		// final_shared_point is the final point in the shared sequence
		int final_shared_index = first_poly.get_next(first_sequence_end, first_increment_direction);
		Point final_shared_point = first_poly.get_point(final_shared_index);

		// Create new polygon out of the unshared points of each merging polygon, with one copy of each endpoint
		// first_forward is notted because merge_lists is traveling over the non-shared points
		// second_sequence_end and second_sequence_start are swapped because we're traveling over the second sequence in reverse order so 
		//  that it combines properly with the first sequence; because of this, second_forward is not notted
		return merge_lists(first_poly, second_poly, first_sequence_start, first_sequence_end, !first_forward, second_sequence_end, 
							second_sequence_start, second_forward, initial_shared_point, final_shared_point);
	}

	// first_poly and second_poly are the polygons being combined
	// first_start, first_end, second_start, and second_end are the indices of the start and end of the sequences of points these polygons 
	//  don't share
	// first_is_forward and second_is_forward indicate whether to iterate forward to cover the non-shared points and avoid the shared ones
	// first_pre_start and first_post_end are the endpoints of the shared sequence; first_pre_start is adjacent to first_start, and 
	//  first_post_end is adjacent to second_start
	private ArrayList<Point> merge_lists(Polygon first_poly, Polygon second_poly, int first_start, int first_end, 
		boolean first_is_forward, int second_start, int second_end, boolean second_is_forward, Point first_pre_start, Point first_post_end)
	{
		ArrayList<Point> result_list = new ArrayList<Point>();
		int current_index = first_start;
		
		// Create an increment to move forward or backward through the list
		int increment_direction;
		if (first_is_forward)
		{
			increment_direction = 1;
		}
		else
		{
			increment_direction = -1;
		}			
		
		result_list.add(first_pre_start);
		while (current_index != first_end)
		{
			result_list.add(first_poly.get_point(current_index));
			current_index = first_poly.get_next(current_index, increment_direction);
		}
		result_list.add(first_poly.get_point(first_end));
		result_list.add(first_post_end);
		
		current_index = second_start;
		if (second_is_forward)
		{
			increment_direction = 1;
		}
		else
		{
			increment_direction = -1;
		}			
		
		while (current_index != second_end)
		{
			result_list.add(second_poly.get_point(current_index));
			current_index = second_poly.get_next(current_index, increment_direction);	
		}
		result_list.add(second_poly.get_point(second_end));
		
		return result_list;
	}
	
	public List<Polygon> get_outline_draw_list()
	{
		return outline_draw_list;
	}

	public List<Polygon> get_draw_list()
	{
		return draw_list;
	}
}
