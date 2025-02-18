/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-04-13 22:22:44 -0500 (Tue, 13 Apr 2010) $
 * $Revision: 12851 $
 *
 * Copyright (C) 2004-2005  The Jmol Development Team
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


import java.applet.Applet;
import java.awt.Event;
import java.awt.Graphics;

import org.jmol.util.Logger;
import org.jmol.smiles.InvalidSmilesException;
import org.jmol.smiles.SmilesMatcher;

/*
 * JmolSmilesApplet is a small applet that can do interesting things with
 * SMILES strings. It is a subset of Jmol with no graphics capability
 * 
 *  find(String pattern, String smiles, boolean isSmarts, boolean isAll)
 *  getLastError()
 *  getRelationship(String smiles1, String smiles2)
 *  reverseChiralit(String smiles)
 *  setDebug(boolean TF)
 *  
 *  
 */
public class JmolSmilesApplet extends Applet {

  @Override
  public void init() {
    System.out.println("JmolSmilesApplet init");
    //System.out.println(find("C[C@@H]1CCCC[C@@H]1C","C[C@@H]1CCCC[C@@H]1C", false, false));
  }

  private static String lastError;

  /**
   * 
   * @return last error generated by the find command
   * 
   */
  public String getLastError() {
    return lastError;
  }
  
  /**
   * set debugging on or off
   * 
   * @param TF
   */
  public void setDebug(boolean TF) {
    Logger.setLogLevel(TF ? Logger.LEVEL_DEBUG : Logger.LEVEL_INFO);
  }
  
  /**
   * When used for student answer checking, the student's response
   * is the SMILES and the key is the PATTERN -- that is, we seek
   * to know if the correct answer (pattern) is IN the student's
   * answer (smiles).  
   * @param pattern
   * @param smiles
   * @param isSmarts
   * @param isAll set TRUE if you want a full count
   * @return    n>0 with isAll TRUE: found n occurances, 
   *               1: found at least 1, 0: not found, -1: error
   */
  public int find(String pattern, String smiles, boolean isSmarts, boolean isAll) {
    lastError = null;
    int ret = -1;
    try {
      SmilesMatcher sm = new SmilesMatcher();
      int[][] result = sm.find(pattern, smiles, isSmarts, !isAll);
      ret = (result == null ? -1 : result.length);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      lastError = InvalidSmilesException.getLastError();
    } catch (Error er) {
      System.out.println(er.getMessage());
    }
    return ret;
  }

  /**
   * compares two SMILES strings
   * 
   * @param smiles1
   * @param smiles2
   * @return  IDENTICAL, ENANTIOMERS, DIASTEREOMERS, CONSTITUTIONAL ISOMERS, NONE, or an error message 
   */
  public String getRelationship(String smiles1, String smiles2) {
    try {
      return (new SmilesMatcher()).getRelationship(smiles1, smiles2);
    } catch (Exception e) {
      return e.toString();
    }  
  }


  /**
   * reverse the chirality throughout a SMILES string
   * 
   * @param smiles
   * @return  reversed chirality SMILES string 
   */
  public String reverseChirality(String smiles) {
    return (new SmilesMatcher()).reverseChirality(smiles);  
  }

  @Override
  public String getAppletInfo() {
    return "JmolSmilesApplet";
  }
  
  @Override
  public void update(Graphics g) {}
  @Override
  public void paint(Graphics g) {}
  @Override
  public boolean handleEvent(Event e) {
    return false;
  }
  
  @Override
  public void destroy() {
    System.out.println("JmolSmilesApplet destroyed");
  }

}
