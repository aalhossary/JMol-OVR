/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-09-13 00:06:10 -0500 (Wed, 13 Sep 2006) $
 * $Revision: 5516 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.shape;

import javajs.awt.Font;

import org.jmol.java.BS;
import org.jmol.modelset.Text;

public abstract class TextShape extends Object2dShape {

  // echo, hover
  
  @Override
  public void setProperty(String propertyName, Object value, BS bsSelected) {
    setPropTS(propertyName, value, bsSelected);
  }

  protected void setPropTS(String propertyName, Object value, BS bsSelected) {
    if ("text" == propertyName) {
      String text = (String) value;
      if (currentObject != null) {
        ((Text) currentObject).setText(text);
      } else if (isAll) {
        for (Text t : objects.values())
          t.setText(text);
      }
      return;
    }

    if ("font" == propertyName) {
      currentFont = (Font) value;
      if (currentObject != null) {
        ((Text) currentObject).setFont(currentFont, true);
        ((Text) currentObject).setFontScale(0);
      } else if (isAll) {
        for (Text t : objects.values())
          t.setFont(currentFont, true);
      }
      return;
    }

    setPropOS(propertyName, value, bsSelected);
  }
  
}

