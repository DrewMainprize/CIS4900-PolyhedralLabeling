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

import java.util.Comparator;

// This class is required so that points can be sorted
public class PointComparator implements Comparator<Point>
{
	// Point a comes before point b if its x-value is less, or if its x-value is the same and its y-value is less, etc.
	// Since the values are doubles, none of the x's should ever be equal.
	public int compare(Point a, Point b)
	{
		if (a.x_point == b.x_point)
		{
			if (a.y_point == b.y_point)
			{
				if (a.z_point == b.z_point)
				{
					return 0;
				}
				else if (a.z_point < b.z_point)
				{
					return -1;
				}
				else 
				{
					return 1;
				}
			}
			else if (a.y_point > b.y_point)
			{
				return -1;
			}
			else 
			{
				return 1;
			}
		}
		else if (a.x_point > b.x_point)
		{
			return -1;
		}
		else 
		{
			return 1;
		}
	}
}
