/*
  Copyright 2006-2011 Stefano Chizzolini. http://www.pdfclown.org

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)

  This file should be part of the source code distribution of "PDF Clown library"
  (the Program): see the accompanying README files for more info.

  This Program is free software; you can redistribute it and/or modify it under the terms
  of the GNU Lesser General Public License as published by the Free Software Foundation;
  either version 3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  either expressed or implied; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this
  Program (see README files); if not, go to the GNU website (http://www.gnu.org/licenses/).

  Redistribution and use, with or without modification, are permitted provided that such
  redistributions retain the above copyright notice, license and disclaimer, along with
  this list of conditions.
*/

package org.pdfclown.documents.contents.colorSpaces;

import java.awt.Paint;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.List;

import org.pdfclown.PDF;
import org.pdfclown.VersionEnum;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.contents.IContentContext;
import org.pdfclown.objects.PdfArray;
import org.pdfclown.objects.PdfDirectObject;
import org.pdfclown.objects.PdfInteger;
import org.pdfclown.objects.PdfName;
import org.pdfclown.objects.PdfNumber;
import org.pdfclown.objects.PdfStream;
import org.pdfclown.util.NotImplementedException;

/**
  ICC-based color space [PDF:1.6:4.5.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.1.1, 04/10/11
*/
// TODO:IMPL improve profile support (see ICC.1:2003-09 spec)!!!
@PDF(VersionEnum.PDF13)
public final class ICCBasedColorSpace
  extends ColorSpace<PdfArray>
{
  // <class>
  // <dynamic>
  // <fields>
	private java.awt.color.ColorSpace cs;
  // </fields>
  // <constructors>
  //TODO:IMPL new element constructor!

  ICCBasedColorSpace(
    PdfDirectObject baseObject
    )
  {
	  super(baseObject);
	  
	  //Let's get the ICC profile!
	  ICC_Profile profile = null;
	  byte[] data = getProfile().getBody().toByteArray();
	  try {
	  	profile = ICC_Profile.getInstance(data);
	  } catch (IllegalArgumentException e) {
	  	//This exception should be quite unlikely to happen, I guess?
	  	System.err.println("Invalid ICC profile?");
	  	//TODO use ALTERNATE space specified in header file
	  	/*
	  	 * If this entry is omitted and the application  does  not  understand  the  ICC  profile  data,  
	  	 * the  color  space  used  is DeviceGray, DeviceRGB, or DeviceCMYK, depending on whether the 
	  	 * value of N is 1, 3, or 4, respectively. [PDF:1.7:4.16]
	  	 */
	  	int N = ((PdfInteger)this.getProfile().getHeader().resolve(PdfName.N)).getIntValue();
	  	switch(N) {
	  	case 1:
	  		cs = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY);
	  	case 3:
	  		cs = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB);
	  	case 4:
	  		cs = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.TYPE_CMYK);
	  	}
	  }
	  if(profile != null)
	  	cs = new ICC_ColorSpace(profile);
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public ICCBasedColorSpace clone(
    Document context
    )
  {throw new NotImplementedException();}

  @Override
  public Color<?> getColor(
    List<PdfDirectObject> components,
    IContentContext context
    )
  {
    return new DeviceNColor(components);
  }

  @Override
  public int getComponentCount()
  {
    return cs.getNumComponents();
  }
  
	private static float[] getComponentValues(
			Color<?> color) {
		List<PdfDirectObject> comps = color.getComponents();
		float[] result = new float[comps.size()];
		for (int i = 0; i < result.length; i++) {
			assert comps.get(i) instanceof PdfNumber;
			PdfNumber<?> number = (PdfNumber<?>)comps.get(i);
			result[i] = number.getFloatValue();
		}
		return result;
	}

/* (non-Javadoc)
 * @see org.pdfclown.documents.contents.colorSpaces.ColorSpace#getDefaultColor()
 * [PDF 1.1] In a Lab or ICCBased color space, the initial color has all components equal to 
	0.0 unless that falls outside the intervals specified by the space’s Range entry, 
	in which case the nearest valid value is substituted.
 */
@Override
  public Color<?> getDefaultColor(
    )
  {
		//TODO: ICC profiles that require different minimum values.
		double[] components = new double[getComponentCount()];
    for(
      int index = 0,
        length = components.length;
      index < length;
      index++
      )
    {components[index] = cs.getMinValue(index);}

    return new DeviceNColor(components);
  }

  @Override
  public Paint getPaint(
    Color<?> color
    )
  {
  	float[] colorArr = getComponentValues(color);
  	int inputSize = color.getComponents().size();
  	int currentSize = this.getComponentCount();
  	assert inputSize >= currentSize;
  	
  	// Deal with inputSize not equal to numComponents in color space.
  	if(inputSize < currentSize) {
  		float[] newColorArr = new float[currentSize];
  		for(int i = 0; i < inputSize; i++) {
  			newColorArr[i] = colorArr[i];
  		}
  		colorArr = newColorArr;
  	}
  	// Convert to RGB representation
		float[] rgb = cs.toRGB(colorArr);
    return new java.awt.Color(rgb[0],rgb[1],rgb[2]);
  }

  public PdfStream getProfile(
    )
  {return (PdfStream)getBaseDataObject().resolve(1);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}