/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Stores credentials of the user in the http session of the user.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class CredentialsStore {
  
  @VisibleForTesting
  static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";
  
  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public CredentialsStore(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  /**
   * Extracts the user credentials from token, encrypts them, and stores them in the http session.
   * 
   * @param token username password token
   */
  public void store(UsernamePasswordToken token) {
    // store encrypted credentials in session
    String credentials = token.getUsername();

    char[] password = token.getPassword();
    if (password != null && password.length > 0) {
      credentials = credentials.concat(":").concat(new String(password));
    }

    credentials = encrypt(credentials);
    requestProvider.get().getSession(true).setAttribute(SCM_CREDENTIALS, credentials);
  }
  
  @VisibleForTesting
  protected String encrypt(String credentials){
    return CipherUtil.getInstance().encode(credentials);
  }
  
}
