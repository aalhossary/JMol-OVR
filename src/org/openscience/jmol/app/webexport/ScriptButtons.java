/* $RCSfile$
 * $Author jonathan gutow$
 * $Date Aug 5, 2007 9:19:06 AM $
 * $Revision$
 *
 * Copyright (C) 2005-2007  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */
package org.openscience.jmol.app.webexport;


import javajs.util.PT;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jmol.i18n.GT;
import org.jmol.viewer.Viewer;

class ScriptButtons extends WebPanel {

  ScriptButtons(Viewer vwr, JFileChooser fc, WebPanel[] webPanels,
      int panelIndex) {
    super(vwr, fc, webPanels, panelIndex);
    panelName = "script_btn";
    listLabel = GT._("These names will be used for button labels");
    //description = "Create a web page containing a text and button pane that scrolls next to a resizable Jmol applet";
  }

  @Override
  JPanel appletParamPanel() {
    SpinnerNumberModel appletSizeModel = new SpinnerNumberModel(WebExport.getScriptButtonPercent(), //initial value
        20, //min
        100, //max
        5); //step size
    appletSizeSpinnerP = new JSpinner(appletSizeModel);
    //panel to hold spinner and label
    JPanel appletSizePPanel = new JPanel();
    appletSizePPanel.add(new JLabel(GT._("% of window for applet width:")));
    appletSizePPanel.add(appletSizeSpinnerP);
    return (appletSizePPanel);
  }

  @Override
  String fixHtml(String html) {
    int size = ((SpinnerNumberModel) (appletSizeSpinnerP.getModel()))
        .getNumber().intValue();
    double leftpercent = 99.5 - size;
    int appletheightpercent = 100;
    int nbuttons = getInstanceList().getModel().getSize();
    if (!allSelectedWidgets().isEmpty())
      appletheightpercent = 85;
    html = PT.rep(html, "@WIDTHPERCENT@", "" + size);
    html = PT.rep(html, "@LEFTPERCENT@", "" + leftpercent);
    html = PT.rep(html, "@NBUTTONS@", "" + nbuttons);
    html = PT.rep(html, "@HEIGHT@", "" + appletheightpercent);
    return html;
  }

  @Override
  String getAppletDefs(int i, String html, StringBuilder appletDefs,
                       JmolInstance instance) {
    //TODO add widgets  Could have pure javascript update of widgets for each view.
    //The widgets should appear below the applet as in Angel's example.  The best
    //way to do this would be to build the widget div as a hidden div in the
    //scrolling region and then just copy it to the display area?
    String name = instance.name;
    String buttonname = instance.javaname;
    String widgetDefs = "";
    int row = 0;
    if (!instance.whichWidgets.isEmpty()) {
      widgetDefs += "<table border = \"0\" width=\"100%\"><tbody><tr>";
      for (int j = 0; j < nWidgets; j++) {
        if (instance.whichWidgets.get(j)) {
          if (row == 3) {
            widgetDefs += "</tr><tr>";
            row = 0;
          }
          widgetDefs += "<td>"
              + theWidgets.widgetList[j].getJavaScript(0, instance)
                  //does nothing? .replace("'", "\'")
                  + "</td>";
          row = row + 1;
        }
      }
      widgetDefs += "</tr></tbody></table>";
    }
    if (i == 0)
      html = PT.rep(html, "@APPLETNAME0@", GT.escapeHTML(buttonname));
    if (useAppletJS) {
      String info = "info for " + name;
      appletDefs.append("\naddAppletButton(" + i + ",'" + buttonname + "',\""
          + name + "\",\"" + info + "\");");
    } else {
      String s = htmlAppletTemplate;
      s = PT.rep(s, "@APPLETNAME0@", GT.escapeHTML(buttonname));
      s = PT.rep(s, "@NAME@", GT.escapeHTML(name));
      s = PT.rep(s, "@LABEL@", GT.escapeHTML(name));
      s = PT.rep(s, "@I@", ""+i);
      s = PT.rep(s, "@WIDGETSTR@", widgetDefs);
      appletDefs.append(s);
    }
    return html;
  }
}
