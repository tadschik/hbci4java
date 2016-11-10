
/*  $Id: ConvertSIZRDHPassport.java,v 1.1 2011/05/04 22:37:45 willuhn Exp $

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
import org.kapott.hbci.passport.HBCIPassportInternal;
import org.kapott.hbci.passport.HBCIPassportRDHNew;

/** <p>Tool zum Konvertieren von SIZ-RDH-Schlüsseldateien in 
    RDHNew-Dateien. SIZ-RDH-Schlüsseldateien werden von HBCI-Software verwendet,
    die auf dem SIZ-HBCI-Kernel basiert (z.B. von <em>StarMoney</em>).</p>
    <p>Die Konvertierung kann auch
    mit dem separat verfügbaren <em>HBCI4Java Passport Editor</em> durchgeführt werden.</p>
    <p>Soll der HBCI-Zugang sowohl mit der ursprünglichen Software als auch parallel
    dazu mit <em>HBCI4Java</em> benutzt werden, so ist eine Konvertierung der Schlüsseldatei
    nicht zu empfehlen. Statt dessen sollte die Schlüsseldatei direkt benutzt werden.
    Dafür kann die Passport-Variante {@link org.kapott.hbci.passport.HBCIPassportSIZRDHFile}
    benutzt werden.</p>
    <p>Siehe dazu auch die Datei <code>README.SIZRDHFile</code></p> 
    <p>Aufgerufen wird dieser Konverter mit
    <pre>java -cp ... org.kapott.hbci.tools.ConvertSIZRDHPassport</pre>
    Es handelt sich um ein interaktives Programm. Nach dem Start wird nach dem
    Dateinamen einer existierenden SIZ-RDH-Schlüsseldatei sowie nach dem Usernamen und 
    Passwort für deren Entschlüsselung gefragt. Anschließend wird nach einem
    <em>neuen(!)</em> Dateinamen für die zu erstellende RDHNew-Passport-Datei
    sowie nach einem Passwort für deren Verschlüsselung gefragt. Nach Beendigung
    des Programmes existiert die RDHNew-Passport-Datei, welche ab sofort benutzt
    werden kann.</p> */
// TODO: ConvertRDHXFilePassport 
public class ConvertSIZRDHPassport
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
