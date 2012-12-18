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
import org.pdfclown.bytes.IBuffer;
import org.pdfclown.documents.Document;
import org.pdfclown.documents.contents.IContentContext;
import org.pdfclown.objects.PdfArray;
import org.pdfclown.objects.PdfDirectObject;
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
	private ICC_ColorSpace cs;
	//final 
  // </fields>
  // <constructors>
  //TODO:IMPL new element constructor!

  ICCBasedColorSpace(
    PdfDirectObject baseObject
    )
  {
	  super(baseObject);
	  //TODO use alternate space specified in header file if profile is not found?
	  //TODO deal with ICC profile exceptions
	  
	  //Let's get the ICC profile!
	  PdfStream profileRef = getProfile();
	  IBuffer buffer = ((PdfStream)profileRef).getBody();
	  byte[] data = buffer.getByteArray(0, buffer.getCapacity());
	  ICC_Profile profile = ICC_Profile.getInstance(data);
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
    return new DeviceNColor(components); // FIXME:temporary hack...
  }

  @Override
  public int getComponentCount()
  {
    if(cs != null) {
    	return cs.getNumComponents();
    } else {
    	return 0; //FIXME: verify -- return what value here?
    }
  }
  
	private static float[] getComponentValues(
			Color<?> color) {
		// TODO:normalize parameters!
		List<PdfDirectObject> comps = color.getComponents();
		float[] result = new float[comps.size()];
		//these should contain PdfReals. TODO; deal with errors?
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
		//TODO normalize!
		double[] components = new double[getComponentCount()];
    for(
      int index = 0,
        length = components.length;
      index < length;
      index++
      )
    {components[index] = 0.0;}

    return new DeviceNColor(components);
  }

  @Override
  public Paint getPaint(
    Color<?> color
    )
  {
    // Convert to RGB representation
		// TODO verify that it works
  	int inputSize = color.getComponents().size();
  	assert this.getComponentCount() == inputSize;
  	if(inputSize != this.getComponentCount()) {
  		//FIXME: copy the N values to fit this colorspace instead of just giving default color
  		color = getDefaultColor();
  	}
		float[] rgb = cs.toRGB(getComponentValues(color));
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