
/*  $Id: HBCIPassportDDVPCSC.java,v 1.1 2011/11/24 21:59:37 willuhn Exp $

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

package org.kapott.hbci.passport;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.PBEParameterSpec;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.datatypes.SyntaxCtr;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidPassphraseException;
import org.kapott.hbci.manager.HBCIKey;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.smartcardio.DDVBankData;
import org.kapott.hbci.smartcardio.DDVCardService;
import org.kapott.hbci.smartcardio.DDVKeyData;
import org.kapott.hbci.smartcardio.SmartCardService;

/**
 * Implementierung eines DDV-Passports, welcher intern die neue Chipkarten-API
 * "javax.smartcardio" von Java 6 verwendet. Die Implementierung basiert auf
 * dem OCF-Code von HBCI4Java 2.5.8.
 */
public class HBCIPassportDDVPCSC extends HBCIPassportDDV
{
    private DDVCardService cardService;
    
    /**
     * ct.
     * @param init
     * @param dummy
     */
    public HBCIPassportDDVPCSC(Properties properties, HBCICallback callback, Object init, int dummy)
    {
      super(properties, callback, init,dummy);
    }

    /**
     * ct.
     * @param init
     */
    public HBCIPassportDDVPCSC(Properties properties, HBCICallback callback, Object init)
    {
      this(properties, callback, init,0);
      
      ObjectInputStream is = null;
      
      try
      {
        ////////////////////////////////////////////////////////////////////////
        // set parameters for initializing card
        this.setUseBio(Integer.parseInt(HBCIUtils.getParam(getParamHeader()+".usebio","-1")));
        this.setUseSoftPin(Integer.parseInt(HBCIUtils.getParam(getParamHeader()+".softpin","-1")));
        this.setSoftPin(new byte[0]);
        this.setPINEntered(false);
        this.setEntryIdx(Integer.parseInt(HBCIUtils.getParam(getParamHeader()+".entryidx","1")));
        //
        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        // init card
        HBCIUtils.log("initializing javax.smartcardio",HBCIUtils.LOG_DEBUG);
        HBCIUtilsInternal.getCallback().callback(this,HBCICallback.NEED_CHIPCARD,HBCIUtilsInternal.getLocMsg("CALLB_NEED_CHIPCARD"),HBCICallback.TYPE_NONE,null);
        
        this.initCT();
        HBCIUtilsInternal.getCallback().callback(this,HBCICallback.HAVE_CHIPCARD,"",HBCICallback.TYPE_NONE,null);
        //
        ////////////////////////////////////////////////////////////////////////
        
        ////////////////////////////////////////////////////////////////////////
        // init basic bank data
        try {
          this.setPort(new Integer(3000));
          this.setFilterType("None");
          this.ctReadBankData();
            
          if (this.askForMissingData(true,true,true,false,false,true,false))
            this.saveBankData();
                
          this.ctReadKeyData();
        }
        catch (HBCI_Exception e1)
        {
          throw e1;
        }
        catch (Exception e)
        {
          throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_PASSPORT_INSTDATAERR"),e);
        }
        //
        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        // read passport file
        String path = HBCIUtils.getParam(getParamHeader()+".path","./");
        this.setFileName(HBCIUtilsInternal.withCounter(path+"pcsc"+getCardId(),getEntryIdx()-1));
        HBCIUtils.log("loading passport data from file "+getFileName(),HBCIUtils.LOG_DEBUG);
        
        File file = new File(this.getFileName());
        if (file.exists() && file.isFile() && file.canRead())
        {
          int retries = Integer.parseInt(HBCIUtils.getParam("client.retries.passphrase","3"));

          while (true) // loop for entering the correct passphrase
          {
            if (this.getPassportKey() == null)
              this.setPassportKey(calculatePassportKey(FOR_LOAD));

            PBEParameterSpec paramspec = new PBEParameterSpec(CIPHER_SALT,CIPHER_ITERATIONS);
            String provider = HBCIUtils.getParam("kernel.security.provider");
            Cipher cipher = provider == null ? Cipher.getInstance("PBEWithMD5AndDES") : Cipher.getInstance("PBEWithMD5AndDES", provider);
            cipher.init(Cipher.DECRYPT_MODE,getPassportKey(),paramspec);
              
            try
            {
              is = new ObjectInputStream(new CipherInputStream(new FileInputStream(file),cipher));
            }
            catch (StreamCorruptedException e1)
            {
              setPassportKey(null); // Passwort resetten
              retries--;
              if (retries<=0)
                throw new InvalidPassphraseException();
            }
            catch (Exception e2)
            {
              throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_PASSPORT_READERR"),e2);
            }
            
            // wir habens
            if (is != null)
            {
              setBPD((Properties)(is.readObject()));
              setUPD((Properties)(is.readObject()));
              setHBCIVersion((String)is.readObject());
              break;
            }
          }
        }
        //
        ////////////////////////////////////////////////////////////////////////
      }
      catch (Exception e)
      {
        // Im Fehlerfall wieder schliessen
        try {
          closeCT();
        }
        catch (Exception ex) {
          HBCIUtils.log(ex);
        }
        
        if (e instanceof HBCI_Exception)
          throw (HBCI_Exception) e;
        
        throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_CTERR"),e);
      }
      finally
      {
        // Close Passport-File
        if (is != null) {
          try {
            is.close();
          }
          catch (Exception e) {
            HBCIUtils.log(e);
          }
        }
      }
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#initCT()
     */
    protected void initCT()
    {
      this.cardService = SmartCardService.createInstance(DDVCardService.class,HBCIUtils.getParam(getParamHeader() + ".pcsc.name", null));
      this.setCID(this.cardService.getCID());
      this.setCardId(this.cardService.getCardId());
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctReadBankData()
     */
    protected void ctReadBankData()
    {
      int idx = this.getEntryIdx()-1;
      DDVBankData bankData = this.cardService.readBankData(idx);
      
      this.setBLZ(bankData.blz);
      this.setCountry(SyntaxCtr.getName(bankData.country));
      this.setHost(bankData.commaddr);
      this.setUserId(bankData.userid);
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctReadKeyData()
     */
    protected void ctReadKeyData()
    {
      this.setSigId(new Long(cardService.readSigId()));
      
      // readKeyData
      DDVKeyData[] keyData=cardService.readKeyData();
      
      this.setInstSigKey(new HBCIKey(
          getCountry(), getBLZ(), getUserId(), 
          Integer.toString(keyData[0].num), Integer.toString(keyData[0].version), 
          null));
      
      this.setInstEncKey(new HBCIKey(
          getCountry(), getBLZ(), getUserId(), 
          Integer.toString(keyData[1].num), Integer.toString(keyData[1].version),
          null));
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctEnterPIN()
     */
    protected void ctEnterPIN()
    {
      if (getUseSoftPin()==1)
        this.cardService.verifySoftPIN(1, this.getSoftPin());
      else
        this.cardService.verifyHardPIN(1);
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctSaveBankData()
     */
    protected void ctSaveBankData()
    {
      int idx = this.getEntryIdx()-1;
      DDVBankData bankData;
      
      bankData=cardService.readBankData(idx);
      bankData.country=SyntaxCtr.getCode(this.getCountry());
      bankData.blz=this.getBLZ();
      bankData.commaddr=this.getHost();
      bankData.userid=this.getUserId();
      cardService.writeBankData(idx,bankData);
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctSaveSigId()
     */
    protected void ctSaveSigId()
    {
      cardService.writeSigId(getSigId().intValue());
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctSign(byte[])
     */
    protected byte[] ctSign(byte[] data)
    {
      return cardService.sign(data);
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctEncrypt()
     */
    protected byte[][] ctEncrypt()
    {
      return cardService.getEncryptionKeys(Integer.parseInt(getInstEncKeyNum()));
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#ctDecrypt(byte[])
     */
    protected byte[] ctDecrypt(byte[] cryptedKey)
    {
      return cardService.decrypt(Integer.parseInt(getInstEncKeyNum()),cryptedKey);
    }
    
    /**
     * @see org.kapott.hbci.passport.HBCIPassportDDV#closeCT()
     */
    protected void closeCT()
    {
      try
      {
        if (this.cardService != null)
          this.cardService.close();
      }
      finally
      {
        this.cardService = null;
      }
    }
}
