/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Valeria
 */
public class LeerHuella extends GUI.CapturarHuella{
    
    private DPFPEnrollment enrollador;
    private DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate plantillaHuella;
    private DPFPVerificationResult resultado = null;
    
        private DPFPFeatureSet caracteristicas = null;
        private boolean BUSCAR = false;
    
    public LeerHuella(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
          try {
            enrollador = DPFPGlobal.getEnrollmentFactory().createEnrollment();            
        } catch (java.lang.UnsatisfiedLinkError | java.lang.NoClassDefFoundError e) {
            setVisible(false);
        }
    }

    @Override
    protected void procesarHuella(DPFPSample sample) {
        super.procesarHuella(sample); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        
        caracteristicas = extractFeatures(sample, (isBUSCAR()) ? DPFPDataPurpose.DATA_PURPOSE_VERIFICATION :DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
    
        if (isBUSCAR()) {
          try {                
                boolean encontrado = false;
                String sql = "SELECT idusuario, nombre, huella FROM usuario WHERE huella is not null";
                ConexionBD con = new ConexionBD();
                con.conectar();
                ResultSet rs = con.CONSULTAR(sql);
                while(rs.next()){
                    resultado = verificador.verify(caracteristicas, DPFPGlobal.getTemplateFactory().createTemplate(rs.getBytes("huella")));
                    if(resultado.isVerified()){
                        System.out.println("HUELLA ES DE "+rs.getString("nombre"));
                        break;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(LeerHuella.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
       if(caracteristicas != null){
            try {
                enrollador.addFeatures(caracteristicas);
            } catch (DPFPImageQualityException e) {
                
            }finally{
                switch(enrollador.getFeaturesNeeded()){
                    case 4:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso0.png")));
                        break;
                    case 3:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso1.png")));                               
                        break;
                    case 2:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso2.png")));
                        break;
                    case 1:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso3.png")));
                        break;
                    case 0:
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso4.png")));
                        break;
                }
                
                switch(enrollador.getTemplateStatus()){
                    case TEMPLATE_STATUS_READY:
                        stop();
                        plantillaHuella = enrollador.getTemplate();
                        setVisible(false);
                        break;

                    case TEMPLATE_STATUS_FAILED:
                        enrollador.clear();
                        stop();
                        plantillaHuella = null;
                        lblPasos.setIcon(new ImageIcon(getClass().getResource("/img/paso0.png")));                        
                        start();
                        break;                                
                    default: break;
                }
                
                
            }
        }
    }

    public boolean isBUSCAR() {
        return BUSCAR;
    }

    public void setBUSCAR(boolean BUSCAR) {
        this.BUSCAR = BUSCAR;
    }

    public DPFPTemplate getPlantillaHuella() {
        return plantillaHuella;
    }

    public void setPlantillaHuella(DPFPTemplate plantillaHuella) {
        this.plantillaHuella = plantillaHuella;
    }
    
    
    
}
