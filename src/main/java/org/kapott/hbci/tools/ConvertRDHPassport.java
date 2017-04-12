
/*  $Id: ConvertRDHPassport.java,v 1.1 2011/05/04 22:37:45 willuhn Exp $

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.AbstractRDHSWPassport;
import org.kapott.hbci.passport.HBCIPassportInternal;
import org.kapott.hbci.passport.HBCIPassportRDHNew;

/** <p>Tool zum Konvertieren von "alten" RDH-Passport-Dateien in "neue"
    RDHNew-Dateien. Die Passport-Variante "<code>RDH</code>" sollte ab sofort
    nicht mehr benutzt werden, sondern statt dessen die Variante
    "<code>RDHNew</code>". Siehe dazu auch die Datei <code>README.RDHNew</code>
    im <em>HBCI4Java</em>-Archiv.</p>
    <p>Die Konvertierung von RDH-Passports in RDHNew-Passports kann auch
    mit dem separat verfÃ¼gbaren HBCI4Java Passport Editor durchgefÃ¼hrt werden.
    Mit diesem Editor kÃ¶nnen darÃ¼ber hinaus RDHNew-Passports auch wieder
    in RDH-Passports konvertiert werden.</p> 
    <p>Bei der <code>RDHNew</code>-Variante hat sich im Vergleich zu <code>RDH</code>
    das Dateiformat der SchlÃ¼sseldateien geÃ¤ndert. Deshalb muss vor Verwendung einer alten
    <code>RDH</code>-SchlÃ¼sseldatei diese erst in das neue Dateiformat konvertiert
    werden. Das geschieht mit diesem Tool. Vor dessen Anwendung sollte sicherheithalber
    ein Backup der aktuellen (alten) RDH-SchlÃ¼sseldatei angelegt werden
    (das sollte aber sowieso vorhanden sein!).</p>
    <p>Aufgerufen wird der Konverter mit
    <pre>java -cp ... org.kapott.hbci.tools.ConvertRDHPassport</pre>
    Es handelt sich um ein interaktives Programm. Nach dem Start wird nach dem
    Dateinamen einer existierenden RDH-Passport-Datei sowie nach dem Passwort
    fÃ¼r deren EntschlÃ¼sselung gefragt. AnschlieÃend wird nach einem
    <em>neuen(!)</em> Dateinamen fÃ¼r die zu erstellende RDHNew-Passport-Datei
    sowie nach einem Passwort fÃ¼r deren VerschlÃ¼sselung gefragt. Nach Beendigung
    des Programmes existiert die RDHNew-Passport-Datei, welche ab sofort benutzt
    werden kann.</p>
    <p>Um eine RDHNew-Passport-Datei zu benutzen, mÃ¼ssen alle HBCI-Parameter, die
    vorher mit "<code>client.passport.RDH.*</code>" gesetzt wurden, jetzt mit
    "<code>client.passport.RDHNew.*</code>" gesetzt werden, und als 
    "<code>client.passport.default</code>" bzw. als Argument zu
    {@link org.kapott.hbci.passport.AbstractHBCIPassport#getInstance(String)}
    muss "<code>RDHNew</code>" angegeben werden.</p>
    <p>Die alte Passport-Datei sollte ab sofort nicht mehr verwendet werden.</p>*/
public class ConvertRDHPassport
{

    private static String readParam(String[] args,int idx,String st)
        throws IOException
    {
        String ret;
        
        System.out.print(st+": ");
        System.out.flush();
        
        if (args.length<=idx) {
            ret=new BufferedReader(new InputStreamReader(System.in)).readLine();
        } else {
            System.out.println(args[idx]);
            ret=args[idx];
        }
        
        return ret;
    }
}
