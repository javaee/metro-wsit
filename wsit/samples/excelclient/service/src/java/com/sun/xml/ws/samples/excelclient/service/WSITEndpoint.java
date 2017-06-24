/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.samples.excelclient.service;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.List;

/**
 * @author Jakub Podlesak
 * @author Arun Gupta
 */
@WebService()
public class WSITEndpoint {
    
    @PersistenceUnit
    private EntityManagerFactory emf;
    
    @Resource
    private UserTransaction utx;
    
    @WebMethod(action="getPatientId")
    public int getPatientId(
            @WebParam(name = "firstname") String firstname,
    @WebParam(name = "surname") String surname,
    @WebParam(name ="dob") String dob,
    @WebParam(name = "ssn") String ssn) {
        int patientId = 0;
        Patient ourGuy;
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            Query query = em.createQuery("SELECT p FROM Patient p WHERE p.ssn = :ssn");
            query.setParameter("ssn", ssn);
            try {
                ourGuy = (Patient)query.getSingleResult();
            } catch (NoResultException nre) {
                ourGuy = null;
            }
            if(ourGuy != null) {
                patientId = ourGuy.getPatientid();
            } else {
                ourGuy = new Patient();
                ourGuy.setFirstname(firstname);
                ourGuy.setSurname(surname);
                ourGuy.setSsn(ssn);
                ourGuy.setDob(dob);
                ourGuy.setPatientid(getNewId(em, "PATIENTID"));
                em.persist(ourGuy);
                patientId = ourGuy.getPatientid();
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        
        return patientId;
    }
    
    @WebMethod(action="getPatientFirstname")
    public String getPatientFirstname(
            @WebParam(name = "patientid") int patientid) {
        Patient ourGuy;
        String result = null;
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            ourGuy = em.find(Patient.class, patientid);
            if(ourGuy != null) {
                result = ourGuy.getFirstname();
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        
        return result;
    }
    
    @WebMethod(action="getPatientSurname")
    public String getPatientSurname(
            @WebParam(name = "patientid") int patientid) {
        Patient ourGuy;
        String result = null;
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            ourGuy = em.find(Patient.class, patientid);
            if(ourGuy != null) {
                result = ourGuy.getSurname();
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        
        return result;
    }
    
    @WebMethod(action="getPatientDOB")
    public String getPatientDOB(
            @WebParam(name = "patientid") int patientid) {
        Patient ourGuy;
        String result = null;
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            ourGuy = em.find(Patient.class, patientid);
            if(ourGuy != null) {
                result = ourGuy.getDob();
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        
        return result;
    }
    
    @WebMethod(action="getPatientSSN")
    public String getPatientSSN(
            @WebParam(name = "patientid") int patientid) {
        Patient ourGuy;
        String result = null;
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            ourGuy = em.find(Patient.class, patientid);
            if(ourGuy != null) {
                result = ourGuy.getSsn();
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        
        return result;
    }
    
    @WebMethod(action="getPatientDiagnosis")
    @SuppressWarnings("unchecked")
    public String getPatientDiagnosis(
            @WebParam(name = "patientid") int patientid) {
        String result = "";
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            utx.begin();
            em.joinTransaction();
            Query query = em.createQuery("SELECT d FROM Diagnosis d WHERE d.patientid = :patientid");
            query.setParameter("patientid", patientid);
            List<Diagnosis> diags = query.getResultList();
            if (diags != null) {
                for (Diagnosis diag : diags) {
                    result += (("".equals(result)) ? "" : ", ") + diag.getDiagcode();
                }
            }
            utx.commit();
        } catch (Exception e) {
            System.out.println("e=" + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        return result;
    }

    private int getNewId(final EntityManager em, final String maxid) {
            Maxid currentMax = em.find(Maxid.class, maxid);
            int result = 1;
            if(currentMax != null) {
                result = currentMax.getMaxval();
                currentMax.setMaxval(result+1);
                em.merge(currentMax);
            } else {
                currentMax = new Maxid();
                currentMax.setId(maxid);
                currentMax.setMaxval(result+1);
                em.persist(currentMax);
            }
            return result;
    }    
    
}
