/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package wspolicy.dispatch.base.server;

import javax.jws.WebService;

@WebService
public class Echo {
    
    public String echo(String yodel) {
        StringBuffer holladrio = new StringBuffer();
        int l = yodel.length();
        for (int i = 0; i <  l; i++) {
            holladrio.append(yodel.substring(i, l));
        }
        return holladrio.toString();
    }
    
}
