/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
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
package org.jmol.modelset;

import javajs.awt.Font;
import javajs.util.P3;
import javajs.util.PT;
import javajs.util.SB;

import org.jmol.shape.Shape;
import org.jmol.util.Txt;
import org.jmol.viewer.JC;
import org.jmol.viewer.Viewer;

public class Text extends Object2d {

  private boolean isEcho;

  public boolean doFormatText;

  public String[] lines;

  public Font font;
  private byte fid;
  private int ascent;
  public int descent;
  private int lineHeight;

  protected int offsetX; // Labels only
  protected int offsetY; // Labels only

  private int textWidth;
  private int textHeight;
  public String text;
  public String textUnformatted;
  
  
  public void setOffset(int offset) {
    //Labels only
    offsetX = JC.getXOffset(offset);
    offsetY = JC.getYOffset(offset);
    pymolOffset = null;
    valign = JC.ECHO_XY;
  }

  private int[] widths;

  private Viewer vwr;

  /**
   * @param vwr 
   * @j2sIgnoreSuperConstructor
   */
  private Text(Viewer vwr) {
    this.vwr = vwr;
    boxXY =  new float[5];
  }

  static public Text newLabel(Viewer vwr, Font font, String text,
                              short colix, short bgcolix, int align, float scalePixelsPerMicron) {
    // for labels and hover
    Text t = new Text(vwr);
    t.set(font, colix, align, true, scalePixelsPerMicron);
    t.setText(text);
    t.bgcolix = bgcolix;
    return t;
  }
  
  public static Text newEcho(Viewer vwr, Font font, String target,
                      short colix, int valign, int align,
                      float scalePixelsPerMicron) {
    Text t = new Text(vwr);
    t.isEcho = true;
    t.set(font, colix, align, false, scalePixelsPerMicron);
    t.target = target;
    t.valign = valign;
    t.z = 2;
    t.zSlab = Integer.MIN_VALUE;
    return t;
  }

  private void set(Font font, short colix, int align, boolean isLabelOrHover,
                   float scalePixelsPerMicron) {
    this.scalePixelsPerMicron = scalePixelsPerMicron;
    this.isLabelOrHover = isLabelOrHover;
    this.colix = colix;
    this.align = align;
    this.setFont(font, isLabelOrHover);
  }

  private void getFontMetrics() {
    descent = font.getDescent();
    ascent = font.getAscent();
    lineHeight = ascent + descent;
  }

  public void setFontFromFid(byte fid) { //labels only
    if (this.fid == fid)
      return;
    fontScale = 0;
    setFont(Font.getFont3D(fid), true);
  }

  public void setText(String text) {
    if (image != null)
      getFontMetrics();
    image = null;
    if (text != null && text.length() == 0)
      text = null;
    if (this.text != null && this.text.equals(text))
      return;
    this.text = textUnformatted = text;
    doFormatText = (isEcho && text != null && (text.indexOf("%{") >= 0 || text
        .indexOf("@{") >= 0));
    if (!doFormatText)
      recalc();
  }

  public Object image;
  public float imageScale = 1;

  public  int boxYoff2;
  
  
  public void setImage(Object image) {
    this.image = image;
    // this.text will be file name
    recalc();
  }

  public void setScale(float scale) {
    imageScale = scale;
    recalc();
  }
  
  public void setFont(Font f3d, boolean doAll) {
    font = f3d;
    if (font == null)
      return;
    getFontMetrics();
    if (!doAll)
      return;
    fid = font.fid;
    recalc();
  }

  public void setFontScale(float scale) {
    if (fontScale == scale)
      return;
    fontScale = scale;
    if (fontScale != 0)
      setFont(vwr.gdata.getFont3DScaled(font, scale), true);
  }

  @Override
  protected void recalc() {
    if (image != null) {
      textWidth = textHeight = 0;
      boxWidth = vwr.apiPlatform.getImageWidth(image) * fontScale * imageScale;
      boxHeight = vwr.apiPlatform.getImageHeight(image) * fontScale * imageScale;
      ascent = 0;
      return;
    }
    if (text == null) {
      text = null;
      lines = null;
      widths = null;
      return;
    }
    if (font == null)
      return;
    lines = PT.split(text, (text.indexOf("\n") >= 0 ? "\n" : "|"));
    textWidth = 0;
    widths = new int[lines.length];
    for (int i = lines.length; --i >= 0;)
      textWidth = Math.max(textWidth, widths[i] = stringWidth(lines[i]));
    textHeight = lines.length * lineHeight;
    boxWidth = textWidth + (fontScale >= 2 ? 16 : 8);
    boxHeight = textHeight + (fontScale >= 2 ? 16 : 8);
  }

  public void setPosition(float scalePixelsPerMicron, float imageFontScaling,
                          boolean isAbsolute, float[] boxXY) {
    if (boxXY == null)
      boxXY = this.boxXY;
    else
      this.boxXY = boxXY;
    setWindow(vwr.gdata.width, vwr.gdata.height, scalePixelsPerMicron);
    if (scalePixelsPerMicron != 0 && this.scalePixelsPerMicron != 0)
      setFontScale(scalePixelsPerMicron / this.scalePixelsPerMicron);
    else if (fontScale != imageFontScaling)
      setFontScale(imageFontScaling);
    if (doFormatText) {
      text = (isEcho ? Txt.formatText(vwr, textUnformatted) : textUnformatted);
      recalc();
    }
    float dx = offsetX * imageFontScaling;
    float dy = offsetY * imageFontScaling;
    xAdj = (fontScale >= 2 ? 8 : 4);
    yAdj = ascent - lineHeight + xAdj;
    if (isLabelOrHover) {
      boxXY[0] = movableX;
      boxXY[1] = movableY;
      if (pymolOffset != null) {
        float pixelsPerAngstrom = vwr.tm.scaleToScreen(z, 1000);
        float pz = pymolOffset[3];
        float dz = (pz < 0 ? -1 : 1) * Math.max(0, Math.abs(pz) - 1)
            * pixelsPerAngstrom;
        z -= (int) dz;
        pixelsPerAngstrom = vwr.tm.scaleToScreen(z, 1000);

        /* for whatever reason, Java returns an 
         * ascent that is considerably higher than a capital X
         * forget leading!
         * ______________________________________________
         *                    leading                      
         *                   ________
         *     X X    
         *      X    ascent
         * __  X X _________ _________         
         * _________ descent 
         *                                   textHeight     
         * _________
         *     X X           lineHeight
         *      X    ascent
         * __  X X__________ _________        ___________        
         * _________ descent  
         *     
         *        
         * 
         */
        // dx and dy are the overall object offset, with text
        dx = getPymolXYOffset(pymolOffset[1], textWidth, pixelsPerAngstrom);
        int dh = ascent - descent;
        dy = -getPymolXYOffset(-pymolOffset[2], dh, pixelsPerAngstrom)
            - (textHeight + dh) / 2;
        
        //dy: added -lineHeight (for one line)
        if (pymolOffset[0] == 1) { 
          // from PyMOL - back to original plan
         dy -= descent;
        }
        
        // xAdj and yAdj are the adjustments for the box itself relative to the text 
        xAdj = (fontScale >= 2 ? 8 : 4);
        yAdj = -descent;
        boxXY[0] = movableX - xAdj;
        boxXY[1] = movableY - yAdj;
        isAbsolute = true;
        boxYoff2 = -2; // empirical fudge factor
      } else {
        boxYoff2 = 0;
      }
      setBoxXY(boxWidth, boxHeight, dx, dy, boxXY, isAbsolute);
    } else {
      setPos(fontScale);
    }
    boxX = boxXY[0];
    boxY = boxXY[1];

    // adjust positions if necessary

    if (adjustForWindow)
      setBoxOffsetsInWindow(/*image == null ? fontScale * 5 :*/0,
          isLabelOrHover ? 16 * fontScale + lineHeight : 0, boxY - textHeight);
    //if (!isAbsolute)
    y0 = boxY + yAdj;
  }

  private float getPymolXYOffset(float off, int width, float ppa) {
    float f = (off < -1 ? -1 : off > 1 ? 0 : (off - 1) / 2);
    // offset
    // -3     -2
    // -2     -1
    // -1      0 absolute, -1 width
    //-0.5    -3/4  width
    //  0     -1/2 width
    // 0.5    -1/4 width
    //  1      0
    //  2      1
    //  3      2
    off = (off < -1 || off > 1 ? off + (off < 0 ? 1 : -1) : 0);
    return f * width + off * ppa;
  }

  private void setPos(float scale) {
    float xLeft, xCenter, xRight;
    boolean is3dEcho = (xyz != null);
    if (valign == JC.ECHO_XY || valign == JC.ECHO_XYZ) {
      float x = (movableXPercent != Integer.MAX_VALUE ? movableXPercent
          * windowWidth / 100 : is3dEcho ? movableX : movableX * scale);
      float offsetX = this.offsetX * scale;
      xLeft = xRight = xCenter = x + offsetX;
    } else {
      xLeft = 5 * scale;
      xCenter = windowWidth / 2;
      xRight = windowWidth - xLeft;
    }

    // set box X from alignments

    boxXY[0] = xLeft;
    switch (align) {
    case JC.TEXT_ALIGN_CENTER:
      boxXY[0] = xCenter - boxWidth / 2;
      break;
    case JC.TEXT_ALIGN_RIGHT:
      boxXY[0] = xRight - boxWidth;
    }

    // set box Y from alignments

    boxXY[1] = 0;
    switch (valign) {
    case JC.ECHO_TOP:
      break;
    case JC.ECHO_MIDDLE:
      boxXY[1] = windowHeight / 2;
      break;
    case JC.ECHO_BOTTOM:
      boxXY[1] = windowHeight;
      break;
    default:
      float y = (movableYPercent != Integer.MAX_VALUE ? movableYPercent
          * windowHeight / 100 : is3dEcho ? movableY : movableY * scale);
      boxXY[1] = (is3dEcho ? y : (windowHeight - y)) + offsetY * scale;
   }

    if (align == JC.TEXT_ALIGN_CENTER)
      boxXY[1] -= (image != null ? boxHeight : xyz != null ? boxHeight 
          : ascent - boxHeight) / 2;
    else if (image != null)
      boxXY[1] -= 0;
    else if (xyz != null)
      boxXY[1] -= ascent / 2;
  }

  public static void setBoxXY(float boxWidth, float boxHeight, float xOffset,
                               float yOffset, float[] boxXY, boolean isAbsolute) {
    float xBoxOffset, yBoxOffset;

    // these are based on a standard |_ grid, so y is reversed.
    if (xOffset > 0 || isAbsolute) {
      xBoxOffset = xOffset;
    } else {
      xBoxOffset = -boxWidth;
      if (xOffset == 0)
        xBoxOffset /= 2;
      else
        xBoxOffset += xOffset;
    }
    if (isAbsolute || yOffset > 0) {
      yBoxOffset = -boxHeight - yOffset;
    } else if (yOffset == 0) {
      yBoxOffset = -boxHeight / 2; // - 2; removed in Jmol 11.7.45 06/24/2009
    } else {
      yBoxOffset = -yOffset;
    }
    boxXY[0] += xBoxOffset;
    boxXY[1] += yBoxOffset;
    boxXY[2] = boxWidth;
    boxXY[3] = boxHeight;
  }
  
  private int stringWidth(String str) {
    int w = 0;
    int f = 1;
    int subscale = 1; //could be something less than that
    if (str == null)
      return 0;
    if (str.indexOf("<su") < 0 && str.indexOf("<color") < 0)
      return font.stringWidth(str);
    int len = str.length();
    String s;
    for (int i = 0; i < len; i++) {
      if (str.charAt(i) == '<') {
        if (i + 8 <= len && 
            (str.substring(i, i + 7).equals("<color ") || str.substring(i, i + 8).equals("</color>"))) {
          int i1 = str.indexOf(">", i);
          if (i1 >= 0) {
            i = i1;
            continue;
          }
        }
        if (i + 5 <= len
            && ((s = str.substring(i, i + 5)).equals("<sub>") || s
                .equals("<sup>"))) {
          i += 4;
          f = subscale;
          continue;
        }
        if (i + 6 <= len
            && ((s = str.substring(i, i + 6)).equals("</sub>") || s
                .equals("</sup>"))) {
          i += 5;
          f = 1;
          continue;
        }
      }
      w += font.stringWidth(str.substring(i, i + 1)) * f;
    }
    return w;
  }

  private float xAdj, yAdj;

  private float y0;

  public P3 pointerPt; // for echo

  public void setXYA(float[] xy, int i) {
    if (i == 0) {
      xy[2] = boxX;
      switch (align) {
      case JC.TEXT_ALIGN_CENTER:
        xy[2] += boxWidth / 2;
        break;
      case JC.TEXT_ALIGN_RIGHT:
        xy[2] += boxWidth - xAdj;
        break;
      default:
        xy[2] += xAdj;
      }
      xy[0] = xy[2];
      xy[1] = y0;
    }
    switch (align) {
    case JC.TEXT_ALIGN_CENTER:
      xy[0] = xy[2] - widths[i] / 2;
      break;
    case JC.TEXT_ALIGN_RIGHT:
      xy[0] = xy[2] - widths[i];
    }
    xy[1] += lineHeight;
  }

  public void appendFontCmd(SB s) {
    s.append("  " + Shape.getFontCommand("echo", font));
    if (scalePixelsPerMicron > 0)
      s.append(" " + (10000f / scalePixelsPerMicron)); // Angstroms per pixel
  }

}
