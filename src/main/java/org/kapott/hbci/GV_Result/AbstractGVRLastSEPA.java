/*  $Id: GVRTermUeb.java,v 1.1 2011/05/04 22:37:47 willuhn Exp $

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

package org.kapott.hbci.GV_Result;


import org.kapott.hbci.passport.HBCIPassportInternal;

/**
 * Rückgabedaten für das Einreichen einer terminierten SEPA-Lastschrift. Beim Einreichen
 * einer terminierten Lastschrift gibt die Bank u.U. eine Auftrags-Identifikationsnummer
 * zurück, die benutzt werden kann, um den Auftrag später zu ändern oder zu löschen.
 */
public abstract class AbstractGVRLastSEPA extends HBCIJobResultImpl {

    private String orderid;

    public AbstractGVRLastSEPA(HBCIPassportInternal passport) {
        super(passport);
    }

    /**
     * Gibt die Auftrags-ID zurück, unter der der Auftrag bei der Bank geführt wird.
     *
     * @return die Auftrags-ID oder <code>null</code>, wenn die Bank keine Auftrags-IDs unterstützt
     */
    public String getOrderId() {
        return orderid;
    }

    /**
     * Speichert die Order-ID.
     *
     * @param orderid
     */
    public void setOrderId(String orderid) {
        this.orderid = orderid;
    }

    /**
     * @see org.kapott.hbci.GV_Result.HBCIJobResultImpl#toString()
     */
    public String toString() {
        return "orderid: " + getOrderId();
    }
}
