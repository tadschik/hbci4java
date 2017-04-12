
/*  $Id: ShowLowlevelGVs.java,v 1.1 2011/05/04 22:37:45 willuhn Exp $

    This file is part of HBCI4Java
    Copyright (C) 2001-2008  Stefan Palme

    HBCI4Java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    HBCI4Java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.kapott.hbci.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIKernelImpl;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.MsgGen;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** <p>Dieses Tool dient zum Anzeigen der Struktur von HBCI-Job-Parametern fÃ¼r das
    Erzeugen von Lowlevel-Jobs. Diese Struktur wird benÃ¶tigt, wenn Jobs Ã¼ber das
    Lowlevel-Interface zum Erzeugen und Parametrisieren von Jobs erzeugt werden. Eine
    ErklÃ¤rung des Unterschieds zwischen High- und Lowlevel-Schnittstelle befindet sich
    in der Dokumentation des Packages <code>org.kapott.hbci.GV</code>. </p>
    <p>Der Aufruf erfolgt durch
    <pre>java org.kapott.hbci.tools.ShowLowlevelGVs [hbciversion]</pre>Ist keine
    <code>hbciversion</code> angegeben, so wird diese Ã¼ber STDIN erfragt.</p>
    <p>Das Tool gibt eine baumartige Struktur aus, welche die Lowlevelnamen der GeschÃ¤ftsvorfÃ¤lle
    sowie die Bezeichnungen fÃ¼r die dazugehÃ¶rigen Lowlevel-Parameter enthÃ¤lt. Die Struktur
    fÃ¼r einen Datensatz beginnt immer mit einer Zeile <pre>jobname:JOBNAME version:VERSION</pre>
    Dabei ist VERSION die Versionsnummer des Lowlevel-Jobs JOBNAME, auf die sich die folgende
    Strukturbeschreibung bezieht. Die Strukturbeschreibung fÃ¼r einen Job endet bei der nÃ¤chsten
    Zeile mit diesem Format bzw. am Ende der Ausgabe.</p>
    <p>In den eigentlichen Beschreibungszeilen kÃ¶nnen Zeilen im Format <pre>GROUP:GROUPNAME {MIN,MAX}</pre>
    folgen. Damit wird beschrieben, dass jetzt eine Gruppe von zusammengehÃ¶rigen Jobparametern folgt.
    Eine solche Gruppe muss mindestens MIN und darf hÃ¶chstens MAX mal als Lowlevel-Parameter auftreten.
    Alle Zeilen, die nicht mit <code>GROUP:</code> beginnen, haben das Format
    <pre>LOWLEVELNAME:DATENTYP {MIN,MAX}</pre>
    LOWLEVELNAME ist dabei der Lowlevelname eines Parameters, wie er beim Setzen von Parametern mit
    {@link org.kapott.hbci.GV.HBCIJob#setParam(String,String)} benutzt werden kann.
    DATENFORMAT ist dabei eine Kurzbezeichnung fÃ¼r den Datentyp, den dieser Parameter annehmen kann.
    MIN und MAX geben an, wie oft dieser Parameter (in seiner Gruppe) mindestens bzw. hÃ¶chstens
    auftauchen darf.</p>
    <p>Folgende Datentypen gibt es zur Zeit:</p>
    <ul>
      <li><code>AN</code> - alphanumerische Daten (Strings)</li>
      <li><code>Bin</code> - binÃ¤re Daten (meist in einem Fremdformat) </li>
      <li><code>Code</code> - wie <code>AN</code></li>
      <li><code>Ctr</code> - LÃ¤nderkennzeichen (meist "DE")</li>
      <li><code>Cur</code> - WÃ¤hrungskennzeichen (meist "EUR")</li>
      <li><code>DTAUS</code> - Daten im DTAUS-Format (alphanumerische Daten im DTAUS-Zeichensatz)</li>
      <li><code>Date</code> - Datumsangaben (in einem Locale-typischen Format)</li>
      <li><code>Dig</code> - nur Ziffern (fÃ¼hrende Nullen erlaubt) </li>
      <li><code>ID</code> - wie <code>AN</code></li>
      <li><code>JN</code> - nur "J" oder "N" (fÃ¼r JA bzw. NEIN) - entspricht also Boolean</li>
      <li><code>Num</code> - ganzzahliger numerischer Wert ohne fÃ¼hrende Nullen</li>
      <li><code>Time</code> - Zeitangabe in einem Locale-typischen Format</li>
      <li><code>Wrt</code> - Angaben von Double-Werten im Format ab.cd (keine Exp.-Schreibweise!)</li>
    </ul>
    <p>Innerhalb einer Anwendung kann mit der Methode
    {@link org.kapott.hbci.manager.HBCIHandler#getSupportedLowlevelJobs()}
    eine Liste aller unterstÃ¼tzten Lowlevel-Jobs in Erfahrung gebracht werden. ZusÃ¤tzlich gibt diese
    Methode zu jedem Jobnamen die Versionsnummer zurÃ¼ck, welche fÃ¼r diesen Job von <em>HBCI4Java</em> benutzt
    werden wird (das hÃ¤ngt von der aktuellen HBCI-Version und dem benutzten Passport ab, kann von
    auÃen also nicht direkt beeinflusst werden). In der Ausgabe dieses Tool kann nun nach einem
    bestimmten Lowlevelnamen eines Jobs und der von <em>HBCI4Java</em> dafÃ¼r verwendeten Versionsnummer gesucht werden.
    Ist der entsprechende Eintrag gefunden, so hat man eine Ãbersicht Ã¼ber alle mÃ¶glichen
    Lowlevel-Jobparameter und wie oft diese auftreten mÃ¼ssen bzw. dÃ¼rfen. 
    Die gleiche Ãbersicht erhÃ¤lt man Ã¼brigens, wenn man innerhalb der Anwendung die Methode
    {@link org.kapott.hbci.manager.HBCIHandler#getLowlevelJobParameterNames(String)}
    aufruft, allerdings fehlen in der Ausgabe dieser Methode die Informationen Ã¼ber die mÃ¶glichen
    HÃ¤ufigkeiten der einzelnen Parameter, dafÃ¼r wird hier automatisch die richtige Versionsnummer
    des Jobs ausgewÃ¤hlt.</p>*/
public class ShowLowlevelGVs
    extends AbstractShowLowlevelData
{
    public static void main(String[] args)
        throws Exception
    {
        String hbciversion;
        if (args.length>=1) {
            hbciversion=args[0];
        } else {
            System.out.print("hbciversion: ");
            System.out.flush();
            hbciversion=new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
        
        HBCIKernelImpl kernel=new HBCIKernelImpl(null,hbciversion);
        MsgGen         msggen=kernel.getMsgGen();
        Document       syntax=msggen.getSyntax();
        
        Element  gvlist=syntax.getElementById("GV");
        NodeList gvnodes=gvlist.getChildNodes();
        int      len=gvnodes.getLength();
        
        for (int i=0;i<len;i++) {
            Node gvrefnode=gvnodes.item(i);
            
            if (gvrefnode.getNodeType()==Node.ELEMENT_NODE) {
                String gvname=((Element)gvrefnode).getAttribute("type");
                showData(gvname,syntax);
            }
        }
    }
}
